package com.example.youtube.olx.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.youtube.olx.databinding.ActivityMainBinding;
import com.example.youtube.olx.firebase.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private SwitchMaterial tipoAcesso;
    private FirebaseAuth auth;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurações inicias
        botaoAcessar = binding.buttonAcesso;
        campoEmail = binding.editCadastroEmail;
        campoSenha = binding.editCadastroSenha;
        tipoAcesso = binding.switchAcesso;

        //Configuração para intent
        intent = new Intent(this, AnunciosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle("OLX");

        //Botão acessar inicial
        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();
                validarAcesso(email, senha);
            }
        });
    }

    // método para validar login do usuário
    private void validarAcesso(String email, String senha) {

        if (!email.isEmpty()) {
            if (!senha.isEmpty()) {
                //Instânciando a autenticação
                auth = ConfiguracaoFirebase.getFirebaseAuth();

                //Verifica o estado switch
                if (tipoAcesso.isChecked()) {

                    //cadastro
                    auth.createUserWithEmailAndPassword(email, senha)
                            .addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(CadastroActivity.this,
                                                        "Cadastrado com sucesso!",
                                                        Toast.LENGTH_SHORT).show();

                                                startActivity(intent);
                                            } else {
                                                String erroExcecao = "";

                                                try {
                                                    throw Objects.requireNonNull(task
                                                            .getException());

                                                } catch (FirebaseAuthWeakPasswordException e) {
                                                    erroExcecao = "Digite uma senha mais forte!";

                                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                                    erroExcecao = "Digite um E-mail válido!";

                                                } catch (FirebaseAuthUserCollisionException e) {
                                                    erroExcecao = "Conta já cadastrada!";

                                                } catch (Exception e) {

                                                    erroExcecao = "ao cadastrar usuário"
                                                            + e.getMessage();
                                                    e.printStackTrace();
                                                }
                                                Toast.makeText(CadastroActivity.this,
                                                        "Erro: " + erroExcecao,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                } else {

                    //Login
                    auth.signInWithEmailAndPassword(email, senha)
                            .addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(CadastroActivity.this,
                                                        "Logado com sucesso !",
                                                        Toast.LENGTH_SHORT).show();

                                                startActivity(intent);

                                            } else {

                                                String erroExcecao = "";

                                                try {
                                                    throw Objects.requireNonNull(task
                                                            .getException());

                                                } catch (FirebaseAuthInvalidUserException e) {

                                                    erroExcecao = "Usuário não cadastrado";

                                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                                    erroExcecao = "Credenciais inválida";

                                                } catch (Exception e) {

                                                    erroExcecao = e.getMessage();
                                                    e.printStackTrace();
                                                }

                                                Toast.makeText(CadastroActivity.this,
                                                                "Erro: " + erroExcecao,
                                                                Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }
                                    });
                }

            } else {
                Toast.makeText(CadastroActivity.this,
                                "Digite sua senha!",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(CadastroActivity.this,
                            "Digite seu E-mail!",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }
}