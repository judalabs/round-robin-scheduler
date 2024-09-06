import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class RandomLoadBalancer implements LoadBalancer {

	private final List<String> instances;

	public RandomLoadBalancer(List<String> instances) {
		if(instances.size() > 10) throw new IllegalArgumentException("maximum instances size: 10");
		this.instances = instances;
	}

	@Override
	public String getInstance() {
		final int actual = (int) (Math.random() * (instances.size()));
		return instances.get(actual);
	}
}
