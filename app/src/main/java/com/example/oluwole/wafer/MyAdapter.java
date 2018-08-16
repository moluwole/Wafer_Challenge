package com.example.oluwole.wafer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.oluwole.wafer.model.DataModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<DataModel> myItems;

    public void setData(ArrayList<DataModel> myItems){
        this.myItems = myItems;
        notifyDataSetChanged();
    }

    public void remove(int position){
        myItems.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView countryName, countryLang, countryCurr;
        private View layout;

        ViewHolder(View view){
            super(view);

            layout = view;
            countryName = view.findViewById(R.id.country_name);
            countryCurr = view.findViewById(R.id.country_currency);
            countryLang = view.findViewById(R.id.country_language);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataModel singleItem = myItems.get(position);

        holder.countryName.setText(singleItem.getName());
        holder.countryLang.setText(singleItem.getLanguages());
        holder.countryCurr.setText(singleItem.getCurrencies());
    }

    @Override
    public int getItemCount() {
        return myItems.size();
    }

}
