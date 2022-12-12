import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new TreeMap<>();
    final static int ROUTES = 1_000;

    public static void main(String[] args) throws InterruptedException {
        final ExecutorService threadPool = Executors.newFixedThreadPool(8);
        final List<Callable<Boolean>> threads = new ArrayList<>();

        for (int i = 0; i < ROUTES; i++) {
            threads.add(() -> {
                String route = generateRoute("RLRFR", 100);
                int countOfR = (int) route
                        .chars()
                        .filter(sym -> sym == 'R')
                        .count();
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(countOfR)) {
                        sizeToFreq.computeIfPresent(countOfR, (k, v) -> v + 1);
                    } else {
                        sizeToFreq.compute(countOfR, (k, v) -> v = 1);
                    }
                }
                return true;
            });
        }
        threadPool.invokeAll(threads);
        threadPool.shutdown();
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
        Integer maxKey = map.keySet().stream()
                .max(Comparator.comparing(map::get))
                .orElse(null);
        int maxValue = Collections.max(sizeToFreq.values());
        System.out.printf("Самое частое количество повторений %s (встретилось %s раз)\n", maxKey, maxValue);

        for (Map.Entry<Integer, Integer> item : sizeToFreq.entrySet()) {
            if (item.getKey().equals(maxKey)) {
                continue;
            } else {
                System.out.printf("- %s (%s раз) \n", item.getKey(), item.getValue());
            }
        }
    }
}