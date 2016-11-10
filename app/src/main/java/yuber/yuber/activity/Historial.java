package yuber.yuber.activity;

public class Historial {

    private String Comentario;
    private String Puntaje;
    private String Costo;
    private String Distancia;
    private String Direccion;
    private String Fecha;
    public Historial() {
    }

    public Historial(String comentario, String puntaje, String costo, String distancia, String direccion, String fecha) {
        Comentario = comentario;
        Puntaje = puntaje;
        Costo = costo;
        Distancia = distancia;
        Direccion = direccion;
        Fecha = fecha;
    }

    public String getComentario() {
        return Comentario;
    }

    public void setComentario(String comentario) {
        Comentario = comentario;
    }

    public String getPuntaje() {
        return Puntaje;
    }

    public void setPuntaje(String puntaje) {
        Puntaje = puntaje;
    }

    public String getCosto() {
        return Costo;
    }

    public void setCosto(String costo) {
        Costo = costo;
    }

    public String getDistancia() {
        return Distancia;
    }

    public void setDistancia(String distancia) {
        Distancia = distancia;
    }

    public String getDireccion() {
        return Direccion;
    }

    public void setDireccion(String direccion) {
        Direccion = direccion;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    @Override
    public String toString() {
        return "{" +
                "\"Comentario\":\"" + Comentario + '\"' +
                ",\"Puntaje\":\"" + Puntaje + '\"' +
                ",\"Costo\":\"" + Costo + '\"' +
                ",\"Distancia\":\"" + Distancia + '\"' +
                ",\"Fecha\":\"" + Fecha + '\"' +
                ",\"Direccion\":\"" + Direccion + '\"' +
                '}';
    }

}
