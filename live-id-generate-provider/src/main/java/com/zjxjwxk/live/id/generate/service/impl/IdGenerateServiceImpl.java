package com.zjxjwxk.live.id.generate.service.impl;

import com.zjxjwxk.live.id.generate.dao.mapper.IdGenerateMapper;
import com.zjxjwxk.live.id.generate.dao.po.IdGeneratePO;
import com.zjxjwxk.live.id.generate.service.IdGenerateService;
import com.zjxjwxk.live.id.generate.service.bo.LocalSeqIdBO;
import com.zjxjwxk.live.id.generate.service.bo.LocalUnSeqIdBO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Xinkang Wu
 * @date 2025/4/28 22:38
 */
@Service
public class IdGenerateServiceImpl implements IdGenerateService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenerateServiceImpl.class);

    /**
     * 有序id段更新阈值
     */
    private static final float UPDATE_SEQ_ID_RATE = 0.75f;

    /**
     * id是否有序，1表示有序
     */
    private static final float SEQ_ID = 1;

    /**
     * 本地有序id段Map（id生成策略=>本地有序id段BO）
     */
    private static final Map<Integer, LocalSeqIdBO> LOCAL_SEQ_ID_BO_MAP = new ConcurrentHashMap<>();

    /**
     * 本地无序id段Map（id生成策略=>本地无序id段BO）
     */
    private static final Map<Integer, LocalUnSeqIdBO> LOCAL_UN_SEQ_ID_BO_MAP = new ConcurrentHashMap<>();

    /**
     * 线程池用于异步刷新id段
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(8, 16, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000),
        r -> {
            Thread thread = new Thread(r);
            thread.setName("id-generate-thread-" + ThreadLocalRandom.current().nextInt(1000));
            return thread;
        });

    /**
     * 信号量用于限制只有一个线程可以同时进行刷新id段操作
     */
    private static final Map<Integer, Semaphore> SEMAPHORE_MAP = new ConcurrentHashMap<>();

    @Resource
    private IdGenerateMapper idGenerateMapper;

    @Override
    public Long getSeqId(Integer id) {
        if (id == null) {
            LOGGER.error("[getSeqId] id is null");
            return null;
        }
        LocalSeqIdBO localSeqIdBO = LOCAL_SEQ_ID_BO_MAP.get(id);
        if (localSeqIdBO == null) {
            LOGGER.error("[getSeqId] localSeqIdBO is null, id is {}", id);
            return null;
        }
        // 检查是否到达阈值，若是则更新id段
        refreshLocalSeqId(localSeqIdBO);
        Long currentNum = localSeqIdBO.getCurrentNum().getAndIncrement();
        if (currentNum > localSeqIdBO.getNextThreshold()) {
            LOGGER.error("[getSeqId] currentNum is over nextThreshold");
            return null;
        }
        return currentNum;
    }

    @Override
    public Long getUnSeqId(Integer id) {
        if (id == null) {
            LOGGER.error("[getUnSeqId] id is null");
            return null;
        }
        LocalUnSeqIdBO localUnSeqIdBO = LOCAL_UN_SEQ_ID_BO_MAP.get(id);
        if (localUnSeqIdBO == null) {
            LOGGER.error("[getUnSeqId] localUnSeqIdBO is null, id is {}", id);
            return null;
        }
        // 从本地id段的队列中获取无序id
        Long unSeqId = localUnSeqIdBO.getIdQueue().poll();
        if (unSeqId == null) {
            LOGGER.error("[getUnSeqId] unSeqId is null, id is {}", id);
            return null;
        }
        // 检查是否到达阈值，若是则更新id段
        refreshLocalUnSeqId(localUnSeqIdBO);
        return unSeqId;
    }

    /**
     * Spring初始化Bean后调用
     */
    @Override
    public void afterPropertiesSet() {
        List<IdGeneratePO> idGeneratePOList = idGenerateMapper.selectAll();
        for (IdGeneratePO idGeneratePO : idGeneratePOList) {
            LOGGER.info("IdGenerateService初始化占用新的id段");
            // 尝试占用id段，并存入本地内存Map
            tryUpdateIdGenerate(idGeneratePO);
            // 为该id生成策略初始化信号量
            SEMAPHORE_MAP.put(idGeneratePO.getId(), new Semaphore(1));
        }
    }

    /**
     * 刷新本地有序id段
     */
    private void refreshLocalSeqId(LocalSeqIdBO localSeqIdBO) {
        long step = localSeqIdBO.getNextThreshold() - localSeqIdBO.getCurrentStart();
        // 检查是否到达更新阈值
        if (localSeqIdBO.getCurrentNum().get() - localSeqIdBO.getCurrentStart() > step * UPDATE_SEQ_ID_RATE) {
            // 根据id生成策略获取信号量
            Semaphore semaphore = SEMAPHORE_MAP.get(localSeqIdBO.getId());
            if (semaphore == null) {
                LOGGER.error("[refreshLocalSeqId] semaphore is null, id is {}", localSeqIdBO.getId());
                return;
            }
            // 使用信号量限制只有一个线程可以同时进行刷新id段操作
            boolean acquireStatus = semaphore.tryAcquire();
            if (acquireStatus) {
                // 异步刷新本地有序id段
                LOGGER.info("开始异步刷新本地有序id段");
                THREAD_POOL_EXECUTOR.execute(() -> {
                    try {
                        IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localSeqIdBO.getId());
                        tryUpdateIdGenerate(idGeneratePO);
                    } catch (Exception e) {
                        LOGGER.error("[refreshLocalSeqId] error is ", e);
                    } finally {
                        // 释放该id生成策略对应的信号量
                        SEMAPHORE_MAP.get(localSeqIdBO.getId()).release();
                        LOGGER.info("异步刷新本地有序id段完成, id is {}", localSeqIdBO.getId());
                    }
                });
            }
        }
    }

    /**
     * 刷新本地无序id段
     */
    private void refreshLocalUnSeqId(LocalUnSeqIdBO localUnSeqIdBO) {
        long begin = localUnSeqIdBO.getCurrentStart(), end = localUnSeqIdBO.getNextThreshold();
        long queueSize = localUnSeqIdBO.getIdQueue().size();
        // 检查是否到达更新阈值（队列中剩余id数量不足该id段的25%）
        if (queueSize < (end - begin) * 0.25) {
            // 根据id生成策略获取信号量
            Semaphore semaphore = SEMAPHORE_MAP.get(localUnSeqIdBO.getId());
            if (semaphore == null) {
                LOGGER.error("[refreshLocalUnSeqId] semaphore is null, id is {}", localUnSeqIdBO.getId());
                return;
            }
            // 使用信号量限制只有一个线程可以同时进行刷新id段操作
            boolean acquireStatus = semaphore.tryAcquire();
            if (acquireStatus) {
                // 异步刷新本地无序id段
                LOGGER.info("开始异步刷新本地无序id段");
                THREAD_POOL_EXECUTOR.execute(() -> {
                    try {
                        IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localUnSeqIdBO.getId());
                        tryUpdateIdGenerate(idGeneratePO);
                    } catch (Exception e) {
                        LOGGER.error("[refreshLocalUnSeqId] error is ", e);
                    } finally {
                        // 释放该id生成策略对应的信号量
                        SEMAPHORE_MAP.get(localUnSeqIdBO.getId()).release();
                        LOGGER.info("异步刷新本地无序id段完成, id is {}", localUnSeqIdBO.getId());
                    }
                });
            }
        }
    }

    /**
     * 尝试占用DB中当前id段，并更新至本地内存Map
     */
    private void tryUpdateIdGenerate(IdGeneratePO idGeneratePO) {
        // 尝试占用DB中当前id段，并更新其为下一id段，避免和其他机器使用同一id段
        idGeneratePO = tryUpdateIdGenerateInDB(idGeneratePO);
        // 将该id段存入本地内存Map
        if (idGeneratePO.getIsSeq() == SEQ_ID) {
            // 有序id，直接将该id段存入本地内存Map
            AtomicLong currentNum = new AtomicLong(idGeneratePO.getCurrentStart());
            LocalSeqIdBO localSeqIdBO = new LocalSeqIdBO(idGeneratePO.getId(), currentNum, idGeneratePO.getCurrentStart(), idGeneratePO.getNextThreshold());
            LOCAL_SEQ_ID_BO_MAP.put(idGeneratePO.getId(), localSeqIdBO);
        } else {
            // 无序id，将id打乱并存入队列中
            long begin = idGeneratePO.getCurrentStart(), end = idGeneratePO.getNextThreshold();
            List<Long> idList = new ArrayList<>((int) (end - begin));
            for (long i = begin; i < end; ++i) {
                idList.add(i);
            }
            Collections.shuffle(idList);
            ConcurrentLinkedQueue<Long> idQueue = new ConcurrentLinkedQueue<>(idList);
            // 最后存入本地内存Map
            LocalUnSeqIdBO localUnSeqIdBO = new LocalUnSeqIdBO(idGeneratePO.getId(), idQueue, idGeneratePO.getCurrentStart(), idGeneratePO.getNextThreshold());
            LOCAL_UN_SEQ_ID_BO_MAP.put(idGeneratePO.getId(), localUnSeqIdBO);
        }
    }

    /**
     * 尝试占用DB中当前id段，并更新DB为下一id段
     * 同步执行，需要较多IO操作
     */
    private IdGeneratePO tryUpdateIdGenerateInDB(IdGeneratePO idGeneratePO) {
        int updateResult = idGenerateMapper.updateCurrentStartById(idGeneratePO.getId(), idGeneratePO.getVersion());
        if (updateResult > 0) {
            return idGeneratePO;
        }
        // 若占用失败，则最多重试3次
        for (int i = 0; i < 3; ++i) {
            // 获取最新版本的idGenerate信息
            idGeneratePO = idGenerateMapper.selectById(idGeneratePO.getId());
            // 尝试占用最新id段，若失败则表示此时有另一机器刚刚占用了该id段
            updateResult = idGenerateMapper.updateCurrentStartById(idGeneratePO.getId(), idGeneratePO.getVersion());
            if (updateResult > 0) {
                return idGeneratePO;
            }
        }
        throw new RuntimeException("[tryUpdateIdGenerateInDB] id段占用失败，id is " + idGeneratePO.getId());
    }
}
