package yuber.yuber.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import yuber.yuber.R;
import yuber.yuber.adapter.HistorialAdapter;

public class HistoricPuntuacionFragment extends Fragment {

    private List<Movie> movieList = new ArrayList<>();
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String JsonHistorial = "JsonHistorial";
    SharedPreferences sharedpreferences;
    private String Ip = "54.213.51.6";
    private String Puerto = "8080";
    private JSONObject datos;
    private JSONObject rec;

    public HistoricPuntuacionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);
        rv.setHasFixedSize(true);

        prepareMovieData();
        HistorialAdapter adapter = new HistorialAdapter(movieList);

        rv.setAdapter(adapter);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        rv.addOnItemTouchListener(new HistoricRecyclerTouchListener(getActivity().getApplicationContext(), rv, new HistoricRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Movie movie = movieList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), movie.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        return rootView;
    }
/*
    private void prepareMovieData() {
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/MisReseñasObtenidas/" + email;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (!response.isEmpty()){
                    agregarItems(response);
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
*/





    private void prepareMovieData() {
        Movie movie = new Movie("23/10/2016", "5 km", "$250");
        movieList.add(movie);


        movie = new Movie("20/010/2016", "4 km", "$200");
        movieList.add(movie);

        movie = new Movie("19/09/2016", "2,5 km", "$125");
        movieList.add(movie);

        movie = new Movie("19/09/2016", "3,5 km", "$175");
        movieList.add(movie);

        movie = new Movie("29/07/2016", "1 km", "$50");
        movieList.add(movie);

        movie = new Movie("19/05/2016", "10 km", "$500");
        movieList.add(movie);

        movie = new Movie("02/05/2016", "12 km", "$600");
        movieList.add(movie);

        movie = new Movie("29/03/2016", "3 km", "$150");
        movieList.add(movie);

        movie = new Movie("19/02/2016", "5 km", "$250");
        movieList.add(movie);

        movie = new Movie("19/01/2016", "1 km", "$50");
        movieList.add(movie);

        movie = new Movie("10/01/2016", "2,2km", "$110");
        movieList.add(movie);

        movie = new Movie("19/07/2015", "2 km", "$100");
        movieList.add(movie);

        movie = new Movie("01/01/2015", "12 km", "$600");
        movieList.add(movie);
    }


    private void agregarItems(String response){
        //El response tiene el JSON
        String titulo = "Comentario: ";
        String subTitulo = "Puntuacion: ";
        String fecha = "fecha";
        try {
            JSONArray arr_strJson = new JSONArray(response);
            Movie movie;
            for (int i = 0; i < arr_strJson.length(); ++i) {
                //limpio las variables para que no vayan concatenando
                titulo = "Comentario: ";
                subTitulo = "Puntuacion: ";
                fecha = "fecha";
                rec = arr_strJson.getJSONObject(i);
                //rec tiene el JSON de una resena
                datos = new JSONObject(rec.toString());
                titulo += (String) datos.getString("reseñaComentario");
                subTitulo += (String) datos.getString("reseñaPuntaje");
                //Agrego a la lista
                movie = new Movie(titulo, subTitulo, fecha);
                movieList.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}