package com.example.youtube.olx.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtube.olx.R;
import com.example.youtube.olx.adapter.AdapterAnunciosPublicos;
import com.example.youtube.olx.databinding.ActivityAnunciosBinding;
import com.example.youtube.olx.firebase.ConfiguracaoFirebase;
import com.example.youtube.olx.helper.RecyclerItemClickListener;
import com.example.youtube.olx.model.Anuncio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

public class AnunciosActivity extends AppCompatActivity {
    private ActivityAnunciosBinding binding;
    private FirebaseAuth auth;
    private RecyclerView recyclerAnunciosPublico;
    private Button buttonRegiao, buttonCategoria;
    private final List<Anuncio> listaAnuncios = new ArrayList<>();
    private AdapterAnunciosPublicos adapterAnunciosPublicos;
    private DatabaseReference database;
    private ValueEventListener valueEventListener;
    private ProgressBar progressBar;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnunciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurações inicias
        recyclerAnunciosPublico = binding.contentAnucioPublico.recyclerAnuncios;
        progressBar = binding.contentAnucioPublico.progressAnuncioPublico;
        buttonRegiao = binding.buttonRegiao;
        buttonCategoria = binding.buttonCategora;
        auth = ConfiguracaoFirebase.getFirebaseAuth();

        //Configurar RecyclerView
        adapterAnunciosPublicos = new AdapterAnunciosPublicos(listaAnuncios,this);
        recyclerAnunciosPublico.setHasFixedSize(true);
        recyclerAnunciosPublico.setAdapter(adapterAnunciosPublicos);
        recuperarAnuncios();

        //Butão Região
        buttonRegiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarPorEstado();
            }
        });

        //Butão categoria
        buttonCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarPorCategoria();
            }
        });

        //Clcik recyclereView
        recyclerAnunciosPublico.addOnItemTouchListener(new RecyclerItemClickListener(this,
                recyclerAnunciosPublico, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Anuncio anuncioSelecionado = listaAnuncios.get(position);

                Intent intent = new Intent(AnunciosActivity.this,
                        DetalhesProdutoActivity.class);
                intent.putExtra("anuncioSelecionado", anuncioSelecionado);
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (auth.getCurrentUser() == null) {
            //Usuário deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);

        } else {
            //Usuário Logado
            menu.setGroupVisible(R.id.group_logado, true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intent = new Intent(this, CadastroActivity.class);

        if (item.getItemId() == R.id.deslogado) {
            startActivity(intent);
            finish();
        }

        if (item.getItemId() == R.id.logado_sair) {
            auth.signOut();
            invalidateOptionsMenu();
            finish();
        }

        if (item.getItemId() == R.id.logado_anuncios) {

            Intent intent2 = new Intent(this, MeusAnunciosActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent2);
        }

        return super.onOptionsItemSelected(item);
    }

    private void filtrarPorEstado(){

        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o estado desejado");

        //Configurar o spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner,null);

        //spinner Estado
        Spinner spinner = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.estados);

        ArrayAdapter<String> adapterEstado = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, estados);

        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterEstado);

        dialogEstado.setView(viewSpinner);

        dialogEstado.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filtroEstado = spinner.getSelectedItem().toString();

                if(!filtroEstado.isEmpty()){
                    recuperarAnunciosPorEstado(filtroEstado);
                    filtrandoPorEstado = true;
                }
            }
        });
        dialogEstado.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
    }

    private void filtrarPorCategoria(){

        if(filtrandoPorEstado){

            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a categoria desejada");

            //Configurar o spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner,null);

            //spinner Estado
            Spinner spinner = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] categoria = getResources().getStringArray(R.array.categorias);

            ArrayAdapter<String> adapterCategorias = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, categoria);

            adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapterCategorias);

            dialogEstado.setView(viewSpinner);

            dialogEstado.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            filtroCategoria = spinner.getSelectedItem().toString();

                            if(!filtroEstado.isEmpty()){
                                recuperarAnunciosPorCategoria(filtroCategoria);
                            }
                        }
                    });
            dialogEstado.setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();

        }else {
            Toast.makeText(this, "Escolha primeiro uma estado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void recuperarAnuncios(){

        listaAnuncios.clear();

        database = ConfiguracaoFirebase.getDatabaseReference()
                .child("anuncios");

        valueEventListener = database.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot estados: snapshot.getChildren()) {
                    for(DataSnapshot categorias: estados.getChildren()){
                        for (DataSnapshot anuncios: categorias.getChildren()){

                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            listaAnuncios.add(anuncio);
                        }
                    }
                }
                Collections.reverse(listaAnuncios);
                progressBar.setVisibility(View.GONE);
                adapterAnunciosPublicos.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void recuperarAnunciosPorEstado(String filtroEstado) {

        listaAnuncios.clear();

        progressBar.setVisibility(View.VISIBLE);

        database = ConfiguracaoFirebase.getDatabaseReference()
                .child("anuncios")
                .child(filtroEstado);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot categorias: snapshot.getChildren()){
                    for (DataSnapshot anuncios: categorias.getChildren()){

                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);
                    }
                }
                Collections.reverse(listaAnuncios);
                progressBar.setVisibility(View.GONE);
                adapterAnunciosPublicos.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void recuperarAnunciosPorCategoria(String filtroCategoria){

        listaAnuncios.clear();

        progressBar.setVisibility(View.VISIBLE);

        database = ConfiguracaoFirebase.getDatabaseReference()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot anunciosCategoria: snapshot.getChildren()){

                        Anuncio anuncio = anunciosCategoria.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);
                }
                Collections.reverse(listaAnuncios);
                progressBar.setVisibility(View.GONE);
                adapterAnunciosPublicos.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onStop() {
        if(valueEventListener!=null){
            database.removeEventListener(valueEventListener);
        }
        super.onStop();
    }
}