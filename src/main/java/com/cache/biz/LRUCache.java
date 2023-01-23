package com.cache.biz;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LRUCache<K, V> extends AbstractCache<K, V> {
    private ConcurrentHashMap<Object, Node> map;
    // 虚拟头尾结点
    private Node first;
    private Node last;
    public LRUCache(Integer capacity) {
        super(capacity);
        first = new Node();// Node内部创建一个空的初始化方法
        last = new Node();
        first.next = last;
        last.pre = first;
        this.map = new ConcurrentHashMap<Object, Node>(capacity);
        // 定时删除
        new Thread(new TimeoutTimerThread()).start();
    }

    public LRUCache() {

        super(2 << 10);
        first = new Node();// Node内部创建一个空的初始化方法
        last = new Node();
        first.next = last;
        last.pre = first;
        this.map = new ConcurrentHashMap<>(2 << 10);
        new Thread(new TimeoutTimerThread()).start();
    }
    private void removeNode(Node node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;
    }

    private void addAfterFirst(Node node) {
        // node 与first.next 关系
        node.next = first.next;
        first.next.pre = node;

        // first 与 node 关系
        first.next = node;
        node.pre = first;

    }

    @Override
    protected Integer getConcurrentHashMapSize() {
        return this.map.size();
    }

    @Override
    protected Object doGet(K key) {
        Node node = map.get(key);
        if (map.isEmpty()) {
            return null;
        }
        if (!map.containsKey(key)) {
            return null;
        }
        if(node != null) {
            // 惰性删除
            long timoutTime = node.cache.getTimeUnit().toSeconds(System.currentTimeMillis()
                    - node.cache.getWriteTime());
            if (node.cache.getExpireTime() < timoutTime) {
                removeNode(node);
                map.remove(node.cache.getKey());
                return null;
            }
            removeNode(node);
            addAfterFirst(node);
        }
        return  (node != null) ? node.cache.getValue() : -1;
    }

    @Override
    protected void doRemove(K key) {
        Node node = map.get(key);
        if (map.isEmpty()) {
            return;
        }
        if (!map.containsKey(key)) {
            return;
        }
        removeNode(node);
        map.remove(key);
    }

    @Override
    protected void doPut(K key, V value, Long expire, TimeUnit timeUnit) {
        Node node = map.get(key);
        if (node != null) { // 新值覆盖旧值
            node.cache.setValue(value);
            removeNode(node);
            addAfterFirst(node);
        }else {// 添加一对新的key value
            if (this.isFull()) {
                // 两件事情,第一件把key从map上删除, 第二件事情节点从双向链表中删掉
                map.remove(last.pre.cache.getKey());
                removeNode(last.pre);
            }
            Cache cache = new Cache();
            cache.setKey(key);
            cache.setValue(value);
            cache.setWriteTime(System.currentTimeMillis());
            cache.setAccessTime(System.currentTimeMillis());
            cache.setHitCount(1);
            cache.setTimeUnit(timeUnit != null ? timeUnit : TimeUnit.SECONDS);
            cache.setExpireTime(expire != null ? expire : 7 * 24 * 60 * 60 * 1000 );
            Node newNode = new Node(cache);
            addAfterFirst(newNode);
            map.put(key, newNode);
        }

    }

    class Node {
        private Cache cache;
        public Node pre;
        public Node next;

        public Node(Cache cache) {
            this.cache = cache;
        }
        public Node() {} // 保留空的构造方法给first和next使用
    }


    /**
     * 处理过期缓存
     */
    class TimeoutTimerThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // 定时删除
                    TimeUnit.SECONDS.sleep(60);
                    expireCache();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 创建多久后，缓存失效
         *
         * @throws Exception
         */
        private void expireCache() throws Exception {
            System.out.println("检测缓存是否过期缓存");
            for (Object key : map.keySet()) {
                Cache cache = map.get(key).cache;
                if (cache.getExpireTime() == null) {
                    continue;
                }
                long timoutTime = cache.getTimeUnit().toSeconds(System.currentTimeMillis()
                        - cache.getWriteTime());
                if (cache.getExpireTime() > timoutTime) {
                    continue;
                }
                System.out.println(" 清除过期缓存 ： " + key);
//                //清除过期缓存
                removeNode(map.get(key));
                map.remove(key);
//                while (first != null) {
//                    System.out.println(first.next.cache.getKey());
//                    first = first.next;
//                }
            }
        }
    }

    public static void main(String[] args) {
        LRUCache lruCache = new LRUCache(5);
        for (int i = 0; i < 5; i++) {
            lruCache.put("lru"+i, "张三"+i);
        }
        lruCache.put("lru5","李四",1L, TimeUnit.SECONDS);

        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(lruCache.get("lru"+i));
        }
    }
}
