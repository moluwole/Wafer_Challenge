package com.example.oluwole.wafer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.oluwole.wafer.model.DataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{

    RecyclerView recyclerView;
    MyAdapter adapter;

    private static final String BASE_URL = "https://restcountries.eu/rest/v2/all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.list_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decorator = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);

        recyclerView.addItemDecoration(decorator);

        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        new GetContent(MainActivity.this).execute();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof MyAdapter.ViewHolder){
            adapter.remove(viewHolder.getAdapterPosition());
        }
    }

    private static class GetContent extends AsyncTask<Void, Void, ArrayList<DataModel>>{

        ProgressDialog progressDialog;
        private WeakReference<MainActivity> myActivity;

        GetContent(MainActivity context) {
            super();
            myActivity = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<DataModel> doInBackground(Void... voids) {

            ArrayList<DataModel> myResult = new ArrayList<>();

            try {

                URL url = new URL(BASE_URL);

                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                InputStream stream = new BufferedInputStream(httpURLConnection.getInputStream());

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }

                JSONArray jsonResult = new JSONArray(builder.toString());

                for (int i = 0; i<jsonResult.length(); i++){
                    JSONObject single_country = jsonResult.getJSONObject(i);

                    String name     = single_country.getString("name");
                    String currency = single_country.getJSONArray("currencies").getJSONObject(0).getString("name");
                    String language = single_country.getJSONArray("languages").getJSONObject(0).getString("name");

                    DataModel dataModel = new DataModel(name, currency, language);
                    myResult.add(dataModel);
                }

                httpURLConnection.disconnect();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return myResult;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(myActivity.get());
            progressDialog.setMessage("Please Wait.....");
            progressDialog.isIndeterminate();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<DataModel> dataModels) {
            progressDialog.dismiss();

            MainActivity mainActivity = myActivity.get();

            mainActivity.adapter = new MyAdapter();
            mainActivity.adapter.setData(dataModels);

            mainActivity.recyclerView.setAdapter(mainActivity.adapter);
        }
    }
}
