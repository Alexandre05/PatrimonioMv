package Atividades;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import Helper.Base64Custon;
import Helper.ConFirebase;
import Helper.Permissoes;
import Mode.Usuario;
import br.com.patrimoniomv.R;

public class Cadastrar extends AppCompatActivity {

    private Button jatenhoC, botaoCadastro;
    private CheckBox campoIsAdmin;
    private EditText campoEmail, campoSenha, campoNome, campoEndereco, campoConfirmaSenha;
    private FirebaseAuth autenticacao;
    private EditText campoCodigoEspecial;
    private DatabaseReference refe;

    private String[] permissoes = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private Usuario usuario;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        Permissoes.validarPermissoes(permissoes, this, 1);
        setContentView(R.layout.activity_cadastrar);

        IniciConpo();


        botaoCadastro.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isAdmin = campoIsAdmin.isChecked();
                String nome = campoNome.getText().toString();
                String endereco = campoEndereco.getText().toString();
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                // String repetirSenha= campoConfirmaSenha.toString();


                if (!nome.isEmpty()) {

                    if (!endereco.isEmpty()) {

                        if (!email.isEmpty()) {

                            if (!senha.isEmpty()) {


                                usuario = new Usuario();

                                usuario.setNome(nome);
                                usuario.setEndereco(endereco);
                                usuario.setEmail(email);
                                usuario.setSenha(senha);
                                usuario.setAdmin(isAdmin);
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {

                                                String token = task.getResult();
                                                usuario.setToken(token);
                                            }

                                        });


                                cadaatrarUsuario();

                            } else {
                                Toast.makeText(Cadastrar.this,
                                        "Digite uma senha!",
                                        Toast.LENGTH_SHORT).show();


                            }

                        } else {

                            Toast.makeText(Cadastrar.this,
                                    "Preencha  o E-mail!",
                                    Toast.LENGTH_SHORT).show();

                        }


                    } else {


                        Toast.makeText(Cadastrar.this,
                                "Preencha  Seu Endereço!",
                                Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(Cadastrar.this,
                            "Preencha  Seu Nome",
                            Toast.LENGTH_SHORT).show();


                }

            }

        });


    }


    public void cadaatrarUsuario() {

        autenticacao = ConFirebase.getReferenciaAutencicacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()

        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Log.d("Cadastro", "Autenticação bem-sucedida");
                    FirebaseUser user = autenticacao.getCurrentUser();


                    user.sendEmailVerification();

                    String idU = Base64Custon.codificarBase64(usuario.getEmail());

                    Toast.makeText(Cadastrar.this,
                            "Sucesso ao cadastrar " +
                                    "Usuario, Verifique Seu Email" +
                                    "Para Novos Acessos",
                            Toast.LENGTH_SHORT).show();
                    ConFirebase.AtualizarNomeUsuario(usuario.getNome());
                    usuario.setIdU(idU);
                    String codigoEspecial = campoCodigoEspecial.getText().toString();
                    usuario.salvarUsuario(codigoEspecial);
                    usuario.salvarUsuario(codigoEspecial);
                    Log.d("Cadastro", "Usuário salvo no Firebase");
                    Usuario.getUsuarioAutal();


                    finish();


                } else {
                    String erroExe = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erroExe = "Atenção," +
                                "Digite Uma Senha Mais Forte";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExe = "Por Favor Digite um E-mail Valido!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erroExe = " Atenção," +
                                "Conta Já Cadastrada";

                    } catch (Exception e) {
                        erroExe = "ao cadastrar usuário" + e.getMessage();
                        e.printStackTrace();

                    }

                    Toast.makeText(Cadastrar.this,
                            "Erro:" + erroExe,
                            Toast.LENGTH_SHORT).show();
//
                }
            }
        });

    }


    public void JaConta(View view) {
        startActivity(new Intent(this, Login.class));

    }


    private void IniciConpo() {
        campoCodigoEspecial = findViewById(R.id.codigoEspecial);
        campoIsAdmin = findViewById(R.id.campoIsAdmin);
        jatenhoC = findViewById(R.id.jatenhoCota);
        campoNome = findViewById(R.id.camPNome);
        campoEndereco = findViewById(R.id.camEm);
        campoEmail = findViewById(R.id.campEm);
        campoSenha = findViewById(R.id.camSem);
        botaoCadastro = findViewById(R.id.BtnCadastrar);


    }

    public static boolean verificarForcaSenha(String campoSenha, String campoRepetirSenha, Cadastrar activity) {
        /*As seguintes regras devem ser obedecidas:
         * Mínimo de oito caracteres;
         * Mínimo de um número;
         * Mínimo de uma letra MAIÚSCULA;
         * Mínimo de uma letra minúscula;
         * Mínimo de um caractere especial;
         * Não pode conter letras repetidas;
         * Não pode conter três números em sequência ou repetidos;
         * */

        boolean contemMaiuscula = false;
        boolean contemMinuscula = false;
        boolean contemNumero = false;
        boolean contemSimbolo = false;
        boolean contemMinimoOitoDigitos = false;
        boolean contemSequenciaNumerica = true;
        boolean contemLetrasRepetidas = true;

        String[] maiusculas = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "Ç"};
        String[] minusculas = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "ç"};
        String[] numeros = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] simbolos = {"(", ")", ",", "<", ".", ">", ";", ":", "/", "?", "@", "#", "$", "%", "&", "*", "-", "_", "+", "=", "§", "[", "{", "]", "}", "~", "^", "|"};
        String[] letrasRepetidas = {"aa", "aA", "Aa", "AA", "bb", "bB", "Bb", "BB", "cc", "cC", "Cc", "CC", "dd", "dD", "Dd", "DD", "ee", "eE", "Ee", "EE", "ff", "fF", "Ff", "FF", "gg", "gG", "Gg", "GG", "hh", "hH", "Hh", "HH", "ii", "iI", "Ii", "II", "jj", "jJ", "Jj", "JJ", "kk", "kK", "Kk", "KK", "ll", "lL", "Ll", "LL", "mm", "mM", "Mm", "MM", "nn", "nN", "Nn", "NN", "oo", "oO", "Oo", "OO", "pp", "pP", "Pp", "PP", "qq", "qQ", "Qq", "QQ", "rr", "rR", "Rr", "RR", "ss", "sS", "Ss", "SS", "tt", "tT", "Tt", "TT", "uu", "uU", "Uu", "UU", "vv", "vV", "Vv", "VV", "ww", "wW", "Ww", "WW", "xx", "xX", "Xx", "XX", "zz", "zZ", "Zz", "ZZ", "çç", "çÇ", "Çç", "ÇÇ"};
        String[] sequenciaNumerica = {"012", "123", "234", "345", "456", "567", "678", "789", "890", "000", "111", "222", "333", "444", "555", "666", "777", "888", "999"};

        String erros = "";

        //Senhas devem ser iguais
        if (campoSenha.equals(campoRepetirSenha)) {
            //Senha deve conter pelo menos oito digitos
            if (campoSenha.length() >= 8) contemMinimoOitoDigitos = true;
            else {
                erros += "Senha deve ter no mínimo oito dígitos";
            }
            //Senha deve conter pelo menos uma letra maiúscula
            int M = 0;
            for (int i = 0; i < maiusculas.length; i++) {
                if (campoSenha.contains(maiusculas[i])) {
                    M = 1;
                    if (M == 1) {
                        contemMaiuscula = true;
                        break;
                    }
                }
            }
            if (M == 0) erros += "\nA senha deve conter pelo menos uma letra maiúscula.";
            //Senha deve conter pelo menos uma letra minúscula
            int m = 0;
            for (int i = 0; i < minusculas.length; i++) {
                if (campoSenha.contains(minusculas[i])) {
                    m = 1;
                    if (m == 1) {
                        contemMinuscula = true;
                        break;
                    }
                }
            }
            if (m == 0) erros += "\nA senha deve conter pelo menos uma letra minúscula. ";

            //Senha deve conter pelo menos um número
            int n = 0;
            for (int i = 0; i < numeros.length; i++) {
                if (campoRepetirSenha.contains(numeros[i])) {
                    n = 1;
                    if (n == 1) {
                        contemNumero = true;
                        break;
                    }
                }
            }
            if (n == 0) erros += "\nA senha deve conter pelo menos um número. ";

            //Senha deve conter pelo menos um caractere especial (),<.>;:/?@#$%&*-_+=§[{]}~^|
            int s = 0;
            for (int i = 0; i < simbolos.length; i++) {
                if (campoSenha.contains(simbolos[i])) {
                    s = 1;
                    if (s == 1) {
                        contemSimbolo = true;
                        break;
                    }
                }
            }
            if (s == 0) erros += "\nA senha deve conter pelo menos um caractere especial. ";

            //Senha não pode conter três números em sequencia, nem repetidos
            int seq = 0;
            for (int i = 0; i < sequenciaNumerica.length; i++) {
                if (campoSenha.contains(sequenciaNumerica[i])) {
                    seq = 1;
                    if (seq == 1)
                        erros += "\nA senha não pode conter números em sequência ou repetidos.";
                    break;
                }
            }
            if (seq == 0) contemSequenciaNumerica = false;
            //Senha não pode conter letras iguais em sequencia
            int ls = 0;
            for (int i = 0; i < letrasRepetidas.length; i++) {
                if (campoSenha.contains(letrasRepetidas[i])) {
                    ls = 1;
                    if (ls == 1) erros += "\nA senha não pode conter letras repetidas.";
                    break;
                }
            }
            if (ls == 0) contemLetrasRepetidas = false;

            //Se todos os critérios forem cumpridos
            if (!contemLetrasRepetidas && contemMaiuscula && contemMinuscula && contemNumero && contemSimbolo && contemMinimoOitoDigitos && !contemSequenciaNumerica) {
                return true;
            } else {
                AlertDialog.Builder alerta = new AlertDialog.Builder(activity);
                alerta.setIcon(R.drawable.ic_launcher_foreground);
                alerta.setTitle("Erro");
                alerta.setMessage(erros);
                alerta.setNeutralButton("Tentar novamente", null);
                alerta.create();
                alerta.show();
            }
        }
        return false;
    }
}

