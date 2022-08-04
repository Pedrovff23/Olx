package com.example.youtube.olx.activity;

import android.content.Intent;
import android.icu.number.Scale;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.youtube.olx.R;
import com.example.youtube.olx.databinding.ActivityDetalhesProdutoBinding;
import com.example.youtube.olx.model.Anuncio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DetalhesProdutoActivity extends AppCompatActivity {
    private ActivityDetalhesProdutoBinding binding;
    private ImageSlider image_slider;
    private TextView textDescricaoDetalhe, textEstadoDetalhe, textPrecoDetalhe, textTituloDetalhe;
    private Button buttonVerTelefone;
    private ArrayList<SlideModel> imageList = new ArrayList<>();
    private Anuncio anuncioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalhesProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configuração inicial
        image_slider = binding.imageSlider;
        textDescricaoDetalhe = binding.textDescricaoDetalhe;
        textEstadoDetalhe = binding.textEstadoDetalhe;
        textPrecoDetalhe = binding.textPrecoDetalhe;
        textTituloDetalhe = binding.textTituloDetalhe;
        buttonVerTelefone = binding.verTelefone;

        //Configurar Toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle("Detalhe Produto");


        //Recuperar anúncio para exibicao
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");

        if(anuncioSelecionado != null){

            textTituloDetalhe.setText(anuncioSelecionado.getTitulo());
            textPrecoDetalhe.setText(anuncioSelecionado.getValor());
            textEstadoDetalhe.setText(anuncioSelecionado.getEstado());
            textDescricaoDetalhe.setText(anuncioSelecionado.getDescricao());

            for(int i = 0; i < anuncioSelecionado.getFotos().size(); i++){
                String url = anuncioSelecionado.getFotos().get(i);
                imageList.add(new SlideModel(url,null,null));
            }
            image_slider.setImageList(imageList, ScaleTypes.CENTER_CROP);
        }

        buttonVerTelefone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vizualizarTelefone();
            }
        });

    }

    private void vizualizarTelefone(){
        Intent i = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel",anuncioSelecionado.getTelefone(), null));
        startActivity(i);
    }
}