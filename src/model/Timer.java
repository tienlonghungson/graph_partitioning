package model;

public class Timer implements Runnable{
    AbstractModel model;

    public Timer(AbstractModel model){
        this.model=model;
    }

    @Override
    public void run() {
        model.stop();
    }
}
