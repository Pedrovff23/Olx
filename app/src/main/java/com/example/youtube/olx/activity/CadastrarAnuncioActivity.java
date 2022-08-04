package com.example.youtube.olx.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.youtube.olx.R;
import com.example.youtube.olx.databinding.ActivityCadastrarAnuncioBinding;
import com.example.youtube.olx.firebase.ConfiguracaoFirebase;
import com.example.youtube.olx.helper.Permissoes;
import com.example.youtube.olx.model.Anuncio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.widget.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class CadastrarAnuncioActivity extends AppCompatActivity implements View.OnClickListener {

    private final String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private final List<String> arrayListFotos = new ArrayList<>();
    private final List<String> listaUrlFotos = new ArrayList<>();
    private ActivityCadastrarAnuncioBinding binding;
    private TextInputEditText editTitulo, editDescricao;
    private CurrencyEditText editValor;
    private Spinner spinnerEstado, spinnerCategoria;
    private ImageView imageCadastro1, imageCadastro2, imageCadastro3;
    private MaskEditText editTelefone;
    private Button cadastrarAnuncio;
    private StorageReference storage;
    private android.app.AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastrarAnuncioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Validar Permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        //Configuração inicial
        editTitulo = binding.editTitulo;
        editValor = binding.editValor;
        editDescricao = binding.editDescricao;
        imageCadastro1 = binding.imageCadastro1;
        imageCadastro2 = binding.imageCadastro2;
        imageCadastro3 = binding.imageCadastro3;
        spinnerCategoria = binding.spinnerCategoria;
        spinnerEstado = binding.spinnerEstado;
        cadastrarAnuncio = binding.buttonCadastrarAnuncio;
        editTelefone = binding.editTelefone;
        storage = ConfiguracaoFirebase.getStorageReference();

        //carregar itens spinner
        carregarDadosSpinner();

        //adicionadno envento de click
        imageCadastro1.setOnClickListener(this);
        imageCadastro2.setOnClickListener(this);
        imageCadastro3.setOnClickListener(this);
        cadastrarAnuncio.setOnClickListener(this);


        //Configura localidade para pt -> português BR -> Brasil
        Locale locale = new Locale("pt", "BR");
        editValor.setLocale(locale);

    }

    private void carregarDadosSpinner() {

        String[] estados = getResources().getStringArray(R.array.estados);
        String[] categorias = getResources().getStringArray(R.array.categorias);

        //categoria
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategorias);

        //estado
        ArrayAdapter<String> adapterEstados = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, estados);

        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstados);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negada");
        builder.setMessage("Para acessar o app é necessário aceiter as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("IntentReset")
    public void escolherImagem(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {

            try {

                //Recuperar imagem
                Uri uriFoto = data.getData();
                String caminhoFoto = uriFoto.toString();

                //Configurar imagem no ImageView
                if (requestCode == 1) {
                    imageCadastro1.setImageURI(uriFoto);
                } else if (requestCode == 2) {
                    imageCadastro2.setImageURI(uriFoto);
                } else if (requestCode == 3) {
                    imageCadastro3.setImageURI(uriFoto);
                }
                arrayListFotos.add(caminhoFoto);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageCadastro1) {
            escolherImagem(1);
        }
        if (v.getId() == R.id.imageCadastro2) {
            escolherImagem(2);
        }
        if (v.getId() == R.id.imageCadastro3) {
            escolherImagem(3);
        }
        if (v.getId() == R.id.buttonCadastrarAnuncio) {
            validarDadosAnuncios();
        }
    }

    private void salvarAnuncio(Anuncio anuncio) {

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Anúncio")
                .setCancelable(false)
                .build();

        alertDialog.show();

        //Salvar imagem no Storage
        for (int i = 0; i < arrayListFotos.size(); i++) {
            String urlImagem = arrayListFotos.get(i);

            int tamanhoDaLista = arrayListFotos.size();
            salvarFotoStorage(urlImagem, tamanhoDaLista, i, anuncio);
        }
    }

    private void salvarFotoStorage(String urlImagem, int tamanhoDaLista, int posicaoFoto,
                                   Anuncio anuncio) {

        //Criar nó no storage
        StorageReference imagemAnuncio = storage.child("imagens").child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem" + posicaoFoto);

        //Fazer upload no arquivo
        UploadTask task = imagemAnuncio.putFile(Uri.parse(urlImagem));
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String urlConvertida = uri.toString();
                        listaUrlFotos.add(urlConvertida);

                        if (tamanhoDaLista == listaUrlFotos.size()) {
                            anuncio.setFotos(listaUrlFotos);
                            anuncio.salvar();

                            alertDialog.dismiss();
                            finish();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload!");
            }
        });

    }

    private Anuncio configurarAnuncio() {

        String estado = spinnerEstado.getSelectedItem().toString();
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String titulo = editTitulo.getText().toString();
        String valor = editValor.getText().toString();
        String telefone = editTelefone.getText().toString();
        String descricao = editDescricao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    private void validarDadosAnuncios() {

        Anuncio anuncio = configurarAnuncio();
        String fone = editTelefone.getUnMasked();

        String valor = String.valueOf(editValor.getRawValue());
        if (arrayListFotos.size() != 0) {
            if (!anuncio.getTitulo().isEmpty()) {
                if (!valor.isEmpty() && !valor.equals("0")) {
                    if (!anuncio.getTelefone().isEmpty() && fone.length() >= 10) {
                        if (!anuncio.getDescricao().isEmpty()) {

                            //Salvar anuncio
                            salvarAnuncio(anuncio);

                        } else {
                            exibirMensagemErro("Preencha o campo descrição!");
                        }
                    } else {
                        exibirMensagemErro("Preencha o campo telefone!");
                    }

                } else {
                    exibirMensagemErro("Preencha o campo valor!");
                }

            } else {
                exibirMensagemErro("Preencha o campo titulo!");
            }
        } else {
            exibirMensagemErro("Selecione ao menos uma foto!");
        }
    }

    private void exibirMensagemErro(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}