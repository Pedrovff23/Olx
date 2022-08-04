package com.example.youtube.olx.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.youtube.olx.R;
import com.example.youtube.olx.model.Anuncio;

import java.util.List;

public class AdapterMeusAnuncios extends RecyclerView.Adapter<AdapterMeusAnuncios.MyViewHolder> {

    private final List<Anuncio> adapterAnuncios;
    private final Context context;

    public AdapterMeusAnuncios(List<Anuncio> adapterAnuncios, Context context) {
        this.adapterAnuncios = adapterAnuncios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_adapter_anuncios,
                        parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String preco = adapterAnuncios.get(position).getValor();
        String nome = adapterAnuncios.get(position).getTitulo();
        String fotoUrl = adapterAnuncios.get(position).getFotos().get(0);

        holder.nome.setText(nome);
        holder.preco.setText(preco);

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(context).load(fotoUrl).into(holder.imagem);
        } else {
            holder.imagem.setImageResource(R.drawable.padrao);
        }
    }

    @Override
    public int getItemCount() {
        return adapterAnuncios.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imagem;
        private final TextView nome;
        private final TextView preco;

        public MyViewHolder(@NonNull View view) {
            super(view);
            imagem = view.findViewById(R.id.imagemAdapter);
            nome = view.findViewById(R.id.nomeAdapter);
            preco = view.findViewById(R.id.precoAdapter);
        }
    }
}
