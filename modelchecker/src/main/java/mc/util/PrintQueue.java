package mc.util;

import mc.webserver.webobjects.LogMessage;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Since we use a blocking queue to handle sending messages to the client, we need a queue for sending messages to the terminal.
 * Lets cheat a little.
 */
public class PrintQueue implements BlockingQueue<LogMessage> {
    @Override
    public boolean add(LogMessage logMessage) {
        logMessage.printToConsole();
        return true;
    }

    @Override
    public boolean offer(LogMessage logMessage) {
        return false;
    }

    @Override
    public LogMessage remove() {
        return null;
    }

    @Override
    public LogMessage poll() {
        return null;
    }

    @Override
    public LogMessage element() {
        return null;
    }

    @Override
    public LogMessage peek() {
        return null;
    }

    @Override
    public void put(LogMessage logMessage) throws InterruptedException {

    }

    @Override
    public boolean offer(LogMessage logMessage, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public LogMessage take() throws InterruptedException {
        return null;
    }

    @Override
    public LogMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends LogMessage> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<LogMessage> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public int drainTo(Collection<? super LogMessage> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super LogMessage> c, int maxElements) {
        return 0;
    }
}
