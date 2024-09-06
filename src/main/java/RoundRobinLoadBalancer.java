import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class RoundRobinLoadBalancer implements LoadBalancer {

	private final List<String> instances;
	private final AtomicInteger current = new AtomicInteger();

	public RoundRobinLoadBalancer(List<String> instances) {
		if(instances.size() > 10) throw new IllegalArgumentException("maximum instances size: 10");
		this.instances = instances;
	}

	@Override
	public String getInstance() {
		final int actual = current.getAndUpdate(getAndUpdate());
		return instances.get(actual);
	}

	private IntUnaryOperator getAndUpdate() {
		return curInstance -> curInstance == instances.size() - 1 ? 0 : curInstance + 1;
	}
}
