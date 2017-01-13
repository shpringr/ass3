package bgu.spl171.net.srv;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//create new task, put in the threadPool and execute

public class ActorThreadPool {

    private final Map<Object, Queue<Runnable>> acts;
    private final ReadWriteLock actsRWLock; //just one write
    private final Set<Object> playingNow;
    private final ExecutorService threads;

    public ActorThreadPool(int threads) {
        this.threads = Executors.newFixedThreadPool(threads);
        acts = new WeakHashMap<>();
        playingNow = ConcurrentHashMap.newKeySet();
        actsRWLock = new ReentrantReadWriteLock();
    }

    public void submit(Object act, Runnable r) {
        synchronized (act) {
            if (!playingNow.contains(act)) { //if act the act have no task to do
                playingNow.add(act);
                execute(r, act);
            } else {
                pendingRunnablesOf(act).add(r);
            }
        }
    }

    public void shutdown() {
        threads.shutdownNow();
    }

    private Queue<Runnable> pendingRunnablesOf(Object act) {

        actsRWLock.readLock().lock();
        Queue<Runnable> pendingRunnables = acts.get(act);
        actsRWLock.readLock().unlock();

        if (pendingRunnables == null) {
            actsRWLock.writeLock().lock();
            acts.put(act, pendingRunnables = new LinkedList<>());
            actsRWLock.writeLock().unlock();
        }
        return pendingRunnables;
    }

    private void execute(Runnable r, Object act) {
        threads.execute(() -> {
            try {
                r.run();
            } finally {
                complete(act);
            }
        });
    }

    private void complete(Object act) {
        synchronized (act) {
            Queue<Runnable> pending = pendingRunnablesOf(act);
            if (pending.isEmpty()) { //if there is more task to do
                playingNow.remove(act);
            } else {
                execute(pending.poll(), act);
            }
        }
    }

}
