package yuber.yuber.activity;

public class temporizadorProv extends Thread {

    public void run(boolean prendido) {
        while(prendido){
            this.esperarXsegundos(5);
            System.out.println("Hola");
        }
    }

    private void esperarXsegundos(int segundos) {
        try {
            Thread.sleep(segundos * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
