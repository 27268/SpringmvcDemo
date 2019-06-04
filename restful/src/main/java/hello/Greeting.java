package hello;

public class Greeting {
    long id;
    String context;

    public Greeting(long id, String context){
        this.id = id;
        this.context = context;
    }
    public String getContext() {
        return context;
    }

    public long getId() {
        return id;
    }
}
