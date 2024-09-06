import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.example.LoadBalancerStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoadBalancerTests {

	@Test
	void shouldThrowWhenReachMaximumInstances() {

		final List<String> instances = List.of(
				"instance1", "instance2", "instance3", "instance4",
				"instance1", "instance2", "instance3", "instance4",
				"instance1", "instance2", "instance3", "instance4");
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new RoundRobinLoadBalancer(instances),
				"maximum instances size: 10");
	}

	@Test
	void shouldBalanceRoundRobinInParallel()  {
		IntStream.range(0, 50).forEach(iteration -> {
			final List<String> instances = List.of("instance1", "instance2", "instance3", "instance4");
			LoadBalancer roundRobin = LoadBalancerFactory.create(LoadBalancerStrategy.ROUND_ROBIN, instances);

			Consumer<ConcurrentHashMap<String, Integer>> asserts = concurrentHashMap -> {
				final HashSet<Integer> values = new HashSet<>(concurrentHashMap.values());

				Assertions.assertEquals(1, values.size());
				Assertions.assertTrue(values.contains(250));
			};
			execute(roundRobin, instances, asserts);
		});
	}

	@Test
	void shouldBalanceRandomInParallel()  {
		IntStream.range(0, 50).forEach(iteration -> {
			final List<String> instances = List.of("instance1", "instance2", "instance3", "instance4");
			LoadBalancer randomBalancer = LoadBalancerFactory.create(LoadBalancerStrategy.RANDOM, instances);
			Consumer<ConcurrentHashMap<String, Integer>> asserts = concurrentHashMap -> {
				final HashSet<Integer> values = new HashSet<>(concurrentHashMap.values());

				System.out.println(concurrentHashMap);
				Assertions.assertNotEquals(1, values.size());
				Assertions.assertEquals(4, concurrentHashMap.keySet().size());
			};
			execute(randomBalancer, instances, asserts);
		});
	}

	private static void execute(LoadBalancer loadBalancer, List<String> instances, Consumer<ConcurrentHashMap<String, Integer>> asserts) {
		ConcurrentHashMap<String, Integer> concurrentHashMap = new ConcurrentHashMap<>(instances.size());
		instances.forEach(instance -> concurrentHashMap.put(instance, 0));
		final ExecutorService parallelClients = Executors.newFixedThreadPool(10);

		final List<Callable<String>> collect = IntStream.range(0, 1000)
				.mapToObj(nonUsed -> (Callable<String>) loadBalancer::getInstance)
				.collect(Collectors.toList());

		final List<Future<String>> futures;
		try {
			futures = parallelClients.invokeAll(collect);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		futures.forEach(f -> {
			try {
				final String key = f.get();
				concurrentHashMap.put(key, concurrentHashMap.get(key) + 1);
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
		parallelClients.shutdown();
		asserts.accept(concurrentHashMap);

	}
}
