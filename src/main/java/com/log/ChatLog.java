package com.log;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ChatLog {
    /**
     * 若实现最大记录是 100，则应该为 99
     */
    private int maxCount;

    public ChatLog(int maxCount) {
        this.maxCount = maxCount;
    }

    public void saveData(Jedis jedis, String redisKey, String msg) {
        Transaction transaction = jedis.multi();
        // 向List头部追加记录
        transaction.lpush(redisKey, msg);
        // 仅保留指定区间内的记录数，删除区间外的记录。下标从 0 开始，即 end 需要最大值 -1
        transaction.ltrim(redisKey, 0, maxCount);
        transaction.exec();
    }

    public List<String> queryData(Jedis jedis, String redisKey) {
        List<String> list = jedis.lrange(redisKey, 0, -1);// end 为 -1 表示到末尾。因为前面插入操作时，限定了存在的记录数
        if (list == null || list.size() == 0) {
            list = new ArrayList<String>();
        }
        return list;
    }
}
