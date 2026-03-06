
package rich.events.api.events;

public interface Cancellable {
    public boolean isCancelled();

    public void cancel();
}

