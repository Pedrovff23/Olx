package com.example.youtube.olx.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.youtube.olx.adapter.AdapterMeusAnuncios;
import com.example.youtube.olx.databinding.ActivityMeusAnunciosBinding;
import com.example.youtube.olx.firebase.ConfiguracaoFirebase;
import com.example.youtube.olx.helper.RecyclerItemClickListener;
import com.example.youtube.olx.model.Anuncio;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MeusAnunciosActivity extends AppCompatActivity {
    private ActivityMeusAnunciosBinding binding;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerAnuncios;
    private DatabaseReference databaseReference;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private AdapterMeusAnuncios adapterMeusAnuncios;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMeusAnunciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurações inicias
        floatingActionButton = binding.buttonAdicionarAnuncio;
        recyclerAnuncios = binding.recyclerAnuncios;
        progressBar = binding.progressMeusAnuncios;

        //Configurar RecyclerView
        adapterMeusAnuncios = new AdapterMeusAnuncios(listaAnuncios,this);
        recyclerAnuncios.setAdapter(adapterMeusAnuncios);
        recyclerAnuncios.setHasFixedSize(true);


        //Toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle("Meus Anúncios");

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CadastrarAnuncioActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //Excluir Anuncios
        excluirAnuncio();
    }

    @Override
    protected void onStart() {
        recuperarAnuncios();
        super.onStart();
    }

    private void recuperarAnuncios(){

        listaAnuncios.clear();
        String idUsuario = ConfiguracaoFirebase.getIdUsuario();

        databaseReference = ConfiguracaoFirebase.getDatabaseReference()
                .child("meus_anuncios")
                .child(idUsuario);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds: snapshot.getChildren()){
                    Anuncio anuncio = ds.getValue(Anuncio.class);
                    listaAnuncios.add(anuncio);
                }
                Collections.reverse(listaAnuncios);
                progressBar.setVisibility(View.INVISIBLE);
                adapterMeusAnuncios.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void excluirAnuncio(){

        //Excluir Anuncios
        recyclerAnuncios.addOnItemTouchListener(new RecyclerItemClickListener(this,
                recyclerAnuncios,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onLongItemClick(View view, int position) {

                        Anuncio anuncioSelecionado = listaAnuncios.get(position);
                        anuncioSelecionado.excluir();
                        listaAnuncios.remove(position);
                        adapterMeusAnuncios.notifyDataSetChanged();
                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }));
    }
}