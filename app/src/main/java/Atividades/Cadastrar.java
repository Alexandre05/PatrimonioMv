package Atividades;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.regex.Pattern;

import Ajuda.Base64Custon;
import Ajuda.ConFirebase;
import Ajuda.Permissoes;
import Modelos.Usuario;
import br.com.patrimoniomv.R;


public class Cadastrar extends AppCompatActivity {

    private Button jatenhoC, botaoCadastro;

    private EditText campoEmail, campoSenha, campoNome, campoEndereco, campoConfirmaSenha,campoSobrenome,
    campoIdade,campoSexo,campoCPF;
    private FirebaseAuth autenticacao;
    private EditText campoCodigoEspecial;
    private DatabaseReference refe;
    private EditText campoPortaria;
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
                String codigoEspecial = campoCodigoEspecial.getText().toString();
                String nome = campoNome.getText().toString();
                String endereco = campoEndereco.getText().toString();
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();
                String repetirSenha = campoConfirmaSenha.getText().toString();
                String portaria = campoPortaria.getText().toString();
                String sobrenome = campoSobrenome.getText().toString();
                String idade = campoIdade.getText().toString();
                String cpf = campoCPF.getText().toString();
                String sexo = campoSexo.getText().toString();
                String tipoUsuario = "membro"; // Valor padrão, caso o usuário não possua código especial


                // Verifique se os campos obrigatórios estão preenchidos
                if (nome.isEmpty() || endereco.isEmpty() || email.isEmpty() || senha.isEmpty() || repetirSenha.isEmpty() || cpf.isEmpty()) {
                    Toast.makeText(Cadastrar.this, "Por favor, preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isCPFValid(cpf)) {
                    Toast.makeText(Cadastrar.this, "Por favor, insira um CPF válido.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // A partir daqui, todos os campos obrigatórios estão preenchidos

                if (validar(senha, repetirSenha)) {
                    if (!codigoEspecial.isEmpty() && codigoEspecial.equals(ConFirebase.CODIGO_ESPECIAL)) {
                        tipoUsuario = "AD";
                    }

                    if (tipoUsuario.equals("AD") || (!portaria.isEmpty() && isPortariaValida(portaria))) {
                        usuario = new Usuario();

                        usuario.setNome(nome);
                        usuario.setEndereco(endereco);
                        usuario.setSexo(sexo);
                        usuario.setEmail(email);
                        usuario.setSenha(senha);
                        usuario.setSobrenome(sobrenome);
                        usuario.setIdade(String.valueOf(Integer.parseInt(idade))); // Converta a idade para int antes de atribuí-la
                        usuario.setCpf(cpf);
                        usuario.setNumeroPortaria(portaria);
                        usuario.setTipo(tipoUsuario); // Definir o tipo do usuário

                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        String token = task.getResult();
                                        usuario.setToken(token);
                                    }
                                });

                        cadastrarUsuario();

                        Intent intent = new Intent(Cadastrar.this, Atualizar.class);
                        startActivity(intent);
                    } else if (!tipoUsuario.equals("AD")) {
                        Toast.makeText(Cadastrar.this,
                                "Por favor, insira a portaria.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Cadastrar.this,
                                "Portaria inválida. Por favor, insira uma portaria válida.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Cadastrar.this,
                            "A senha não é válida!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void criarUsuarioFirebaseAuth() {
        autenticacao = ConFirebase.getReferenciaAutencicacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Cadastro", "Autenticação bem-sucedida");
                            FirebaseUser user = autenticacao.getCurrentUser();
                            user.sendEmailVerification();

                            String idU = Base64Custon.codificarBase64(usuario.getEmail());
                            Toast.makeText(Cadastrar.this, "Sucesso ao cadastrar " + "Usuario, Verifique Seu Email" + "Para Novos Acessos", Toast.LENGTH_SHORT).show();
                            ConFirebase.AtualizarNomeUsuario(usuario.getNome());
                            usuario.setIdU(idU);
                            String codigoEspecial = campoCodigoEspecial.getText().toString();
                            usuario.salvarUsuario(codigoEspecial);

                            if (!codigoEspecial.isEmpty() && codigoEspecial.equals(ConFirebase.CODIGO_ESPECIAL)) {
                                // Usuário AD, defina o status como "aprovado"
                                usuario.setStatus("aprovado");
                                usuario.salvarUsuario(codigoEspecial);
                            } else {
                                // Outro tipo de usuário, defina o status como "pendente"
                                usuario.setStatus("pendente");
                                usuario.salvarUsuario(codigoEspecial);
                            }

                            // Adicionar o CPF ao nó "cpfs" vinculado ao ID do usuário
                            DatabaseReference cpfRef = FirebaseDatabase.getInstance().getReference("cpfs");
                            cpfRef.child(usuario.getCpf()).setValue(usuario.getIdU());

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
                        }
                    }
                });
    }


    private void cadastrarUsuario() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
        DatabaseReference cpfRef = FirebaseDatabase.getInstance().getReference("cpfs");

        // Verificar se o ID do usuário já existe
        Query queryId = usersRef.orderByKey().equalTo(String.valueOf(usuario.getIdU()));
        queryId.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // ID do usuário já existe
                    Toast.makeText(Cadastrar.this, "ID do usuário já cadastrado.", Toast.LENGTH_SHORT).show();
                } else {
                    // ID do usuário não existe, verificar o CPF
                    Query queryCpf = usersRef.orderByChild("cpf").equalTo(usuario.getCpf());
                    queryCpf.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // CPF já existe em "usuarios"
                                Toast.makeText(Cadastrar.this, "CPF já cadastrado.", Toast.LENGTH_SHORT).show();
                            } else {
                                // CPF não existe em "usuarios", verificar em "cpfs"
                                cpfRef.child(usuario.getCpf()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // CPF já existe em "cpfs"
                                            Toast.makeText(Cadastrar.this, "CPF já cadastrado.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // CPF não existe, pode criar o usuário
                                            criarUsuarioFirebaseAuth();

                                            // Adicionar o CPF ao nó "cpfs" vinculado ao ID do usuário
                                            cpfRef.child(usuario.getCpf()).setValue(usuario.getIdU());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Erro ao consultar o banco de dados
                                        Log.d("DatabaseError", "Erro ao consultar o banco de dados: " + databaseError.getMessage());
                                        Toast.makeText(Cadastrar.this, "Erro ao consultar o banco de dados.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("DatabaseError", "Erro ao consultar o banco de dados: " + databaseError.getMessage());
                            // Erro ao consultar o banco de dados
                            Toast.makeText(Cadastrar.this, "Erro ao consultar o banco de dados.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Erro ao consultar o banco de dados
                Log.d("DatabaseError", "Erro ao consultar o banco de dados: " + databaseError.getMessage());
                Toast.makeText(Cadastrar.this, "Erro ao consultar o banco de dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }








    public void JaConta(View view) {
        startActivity(new Intent(this, Login.class));

    }
    public static boolean isCPFValid(String CPF) {
        if (CPF.equals("00000000000") ||
                CPF.equals("11111111111") ||
                CPF.equals("22222222222") || CPF.equals("33333333333") ||
                CPF.equals("44444444444") || CPF.equals("55555555555") ||
                CPF.equals("66666666666") || CPF.equals("77777777777") ||
                CPF.equals("88888888888") || CPF.equals("99999999999") ||
                (CPF.length() != 11))
            return(false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        try {
            sm = 0;
            peso = 10;
            for (i=0; i<9; i++) {
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else dig10 = (char)(r + 48);

            sm = 0;
            peso = 11;
            for(i=0; i<10; i++) {
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else dig11 = (char)(r + 48);

            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
                return(true);
            else return(false);
        } catch (InputMismatchException erro) {
            return(false);
        }
    }


    private void IniciConpo() {
        campoCodigoEspecial = findViewById(R.id.codigoEspecial);

        jatenhoC = findViewById(R.id.jatenhoCota);
        campoPortaria = findViewById(R.id.campoNumeroPortaria);
        campoNome = findViewById(R.id.camPNome);
        campoEndereco = findViewById(R.id.camEm);
        campoEmail = findViewById(R.id.campEmail);
        campoSenha = findViewById(R.id.camSem);
        botaoCadastro = findViewById(R.id.BtnCadastrar);
        campoConfirmaSenha=findViewById(R.id.RecamSem);
        campoSobrenome = findViewById(R.id.camPSobrenome);
        campoIdade = findViewById(R.id.camIdade);
        campoCPF = findViewById(R.id.camCPF);
        campoSexo = findViewById(R.id.camSexo);


    }
    private boolean isPortariaValida(String portaria) {
        // Aqui você pode verificar a portaria fornecida de acordo com suas regras.
        // Por exemplo, você pode verificar se a portaria está presente em uma lista de portarias válidas.
        List<String> portariasValidas = Arrays.asList("123", "456", "789");
        return portariasValidas.contains(portaria);
    }
    public static boolean verificarForcaSenha(String campoSenha, String campoRepetirSenha, Cadastrar activity) {
        String erros = "";

        if (campoSenha.equals(campoRepetirSenha)) {
            Pattern minOitoDigitos = Pattern.compile(".{8,}");
            Pattern maiuscula = Pattern.compile("[A-ZÇ]");
            Pattern minuscula = Pattern.compile("[a-zç]");
            Pattern numero = Pattern.compile("\\d");
            Pattern simbolo = Pattern.compile("[^a-zA-Z0-9çÇ]");
            Pattern sequenciaNumerica = Pattern.compile("(012|123|234|345|456|567|678|789|890|000|111|222|333|444|555|666|777|888|999)");
            Pattern letrasRepetidas = Pattern.compile("(.)\\1");

            boolean cumpreRegras = minOitoDigitos.matcher(campoSenha).find() &&
                    maiuscula.matcher(campoSenha).find() &&
                    minuscula.matcher(campoSenha).find() &&
                    numero.matcher(campoSenha).find() &&
                    simbolo.matcher(campoSenha).find() &&
                    !sequenciaNumerica.matcher(campoSenha).find() &&
                    !letrasRepetidas.matcher(campoSenha).find();

            if (cumpreRegras) {
                return true;
            } else {
                erros = "A senha deve conter:\n" +
                        "- Pelo menos 8 caracteres\n" +
                        "- Pelo menos uma letra maiúscula\n" +
                        "- Pelo menos uma letra minúscula\n" +
                        "- Pelo menos um número\n" +
                        "- Pelo menos um caractere especial\n" +
                        "- Não pode conter números em sequência ou repetidos\n" +
                        "- Não pode conter letras repetidas";
            }
        } else {
            erros = "As senhas não são iguais.";
        }

        AlertDialog.Builder alerta = new AlertDialog.Builder(activity);
        alerta.setIcon(R.drawable.ic_launcher_foreground);
        alerta.setTitle("Erro");
        alerta.setMessage(erros);
        alerta.setNeutralButton("Tentar novamente", null);
        alerta.create();
        alerta.show();

        return false;
    }


    public boolean validar(String senha, String confirmaSenha) {
        if (!senha.isEmpty() && !confirmaSenha.isEmpty()) {
            if (senha.equals(confirmaSenha)) {
                if (verificarForcaSenha(senha, confirmaSenha, this)) {
                    return true;
                } else {
                    Toast.makeText(Cadastrar.this,
                            "A senha não atende aos requisitos de segurança!",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(Cadastrar.this,
                        "As senhas não são iguais!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(Cadastrar.this,
                    "Por favor, insira a senha e a confirmação da senha!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}

