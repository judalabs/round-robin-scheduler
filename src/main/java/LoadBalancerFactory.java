import java.util.List;

import org.example.LoadBalancerStrategy;

public class LoadBalancerFactory {

	public static LoadBalancer create(LoadBalancerStrategy loadBalancerStrategy, List<String> instances) {

		return switch (loadBalancerStrategy) {
			case RANDOM -> new RandomLoadBalancer(instances);
			case ROUND_ROBIN -> new RoundRobinLoadBalancer(instances);
		};
	}
}
