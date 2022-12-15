import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new TreeMap<>();
    final static int ROUTES = 1000;

    public static void main(String[] args) throws InterruptedException {
        final Thread topRatingSizeThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    synchronized (sizeToFreq) {
                        sizeToFreq.wait();
                        if (!sizeToFreq.isEmpty()) {
                            Map.Entry<Integer, Integer> maxSize = Collections.max(
                                    sizeToFreq.entrySet(), Map.Entry.comparingByValue());
                            System.out.printf(
                                    "Максимальная частота повторений: %s (%s раз) \n",
                                    maxSize.getKey(),
                                    maxSize.getValue());
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        topRatingSizeThread.start();

        final ExecutorService threadPool = Executors.newFixedThreadPool(3);
        final List<Callable<Boolean>> threads = new ArrayList<>();

        for (int i = 0; i < ROUTES; i++) {
            threads.add(() -> {
                String route = generateRoute("RLRFR", 100);
                int countOfR = (int) (route
                        .chars()
                        .filter(sym -> sym == 'R')
                        .count());
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(countOfR)) {
                        sizeToFreq.computeIfPresent(countOfR, (k, v) -> v + 1);
                    } else {
                        sizeToFreq.compute(countOfR, (k, v) -> v = 1);
                    }
                    sizeToFreq.notify();
                }
                return true;
            });
        }
        threadPool.invokeAll(threads);
        threadPool.shutdown();
        topRatingSizeThread.interrupt();
        display(sizeToFreq);
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    public static void display(Map<Integer, Integer> map) {
        if (map.isEmpty()) {
            System.out.println("Данные отсутствуют");
        } else {
            final int maxValue = Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getValue();
            final Set<Map.Entry<Integer, Integer>> maxSize = new HashSet<>();
            final Set<Map.Entry<Integer, Integer>> otherSize = new HashSet<>();
            map.entrySet().forEach(item -> {
                if (item.getValue().equals(maxValue)) {
                    maxSize.add(item);
                } else {
                    otherSize.add(item);
                }
            });
            maxSize.forEach(item -> {
                System.out.printf("Самое частое количество повторений %s (встретилось %s раз)\n", item.getKey(), item.getValue());
            });
            if (otherSize.size() > 0) {
                otherSize.forEach(item -> {
                    System.out.printf("- %s (%s раз) \n", item.getKey(), item.getValue());
                });
            }
        }
    }
}

