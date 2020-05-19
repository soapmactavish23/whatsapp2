package com.example.whatsapp2.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp2.adapter.MensagensAdapter;
import com.example.whatsapp2.config.ConfiguracaoFirebase;
import com.example.whatsapp2.helper.Base64Custom;
import com.example.whatsapp2.helper.UsuarioFirebase;
import com.example.whatsapp2.model.Conversa;
import com.example.whatsapp2.model.Grupo;
import com.example.whatsapp2.model.Mensagem;
import com.example.whatsapp2.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp2.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView txtNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private EditText txtMensagem;
    private ImageView imgCamera;
    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> listaMensagem = new ArrayList<>();
    private DatabaseReference databaseReference;
    private DatabaseReference mensagemRef;
    private StorageReference storageReference;
    private ChildEventListener childEventListenerMensagens;
    private static final int SELECAO_CAMERA  = 100;

    //Id usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Grupo grupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configurar a Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuracoes Iniciais
        txtNome = findViewById(R.id.txtNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageViewFotoChat);
        txtMensagem = findViewById(R.id.txtMsg);
        imgCamera = findViewById(R.id.imgCamera);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);

        //Recuperar dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getUsuarioLogado();
        usuarioRemetente.setId(idUsuarioRemetente);

        //Recuperar dados do usuario destinatario
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                txtNome.setText(grupo.getNome());
                idUsuarioDestinatario = grupo.getId();

                String foto = grupo.getFoto();
                if(foto != null){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this).load(url).into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            }else{
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                txtNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if(foto != null){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }
        }

        //Configurar o adapter
        adapter = new MensagensAdapter(listaMensagem, getApplicationContext());

        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);

        databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        mensagemRef = databaseReference.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagemRef.removeEventListener(childEventListenerMensagens);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;

            try{

                imagem = (Bitmap) data.getExtras().get("data");

                if(imagem != null){
                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                    byte[] dadosImagem = baos.toByteArray();

                    //Criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //Configurar referencia do firebase
                    StorageReference imgRef = storageReference.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imgRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Erro", "Erro ao fazer upload" + e.getMessage());
                            Toast.makeText(
                                    ChatActivity.this,
                                    "Erro ao fazer upload da imagem: "+ e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Possivel Erro
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri downloadUrl = uri.getResult();

                            if(usuarioDestinatario != null){

                                Mensagem mensagem = new Mensagem();
                                mensagem.setIdUsuario(idUsuarioRemetente);
                                mensagem.setMensagem("imagem.jpeg");
                                mensagem.setImagem(downloadUrl.toString());

                                //Salvar mensagem para o remetente
                                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                //Salvar mensagem para o destinatario
                                salvarMensagem( idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                            }else{

                                for(Usuario membro: grupo.getMembros()){

                                    String idRemetenteGroup = Base64Custom.codificarBase64(membro.getEmail());
                                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                    mensagem.setMensagem("imagem");
                                    mensagem.setNome(usuarioRemetente.getNome());
                                    mensagem.setImagem(downloadUrl.toString());

                                    //salvar mensagem para o membro
                                    salvarMensagem(idRemetenteGroup, idUsuarioDestinatario, mensagem );

                                    //Salvar conversa
                                    salvarConversa( idRemetenteGroup, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);

                                }

                            }

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Sucesso ao enviar da imagem",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void enviarMensagem(View view){
        String msg = txtMensagem.getText().toString();
        if(!msg.isEmpty()){
            if(usuarioDestinatario != null){
                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(msg);
                //Salvar mensagem para o remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //Salvar mensagem para o destinatario
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //Salvar conversa remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);

                //Salvar conversa destinatario
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false );
            }else{

                for(Usuario membro: grupo.getMembros()){

                    String idRemetenteGroup = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(msg);
                    mensagem.setNome(usuarioRemetente.getNome());

                    //salvar mensagem para o membro
                    salvarMensagem(idRemetenteGroup, idUsuarioDestinatario, mensagem );

                    //Salvar conversa
                    salvarConversa( idRemetenteGroup, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);

                }
            }
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Digite uma mensagem para enviar",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //Limpar texto
        txtMensagem.setText("");

    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup ){

        //Salvar conversa remetente
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente( idRemetente );
        conversaRemetente.setIdDestinatario( idDestinatario );
        conversaRemetente.setUltimaMensagem( msg.getMensagem() );

        if ( isGroup ){//conversa de grupo
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo( grupo );
        }else {//Conversa normal
            conversaRemetente.setUsuarioExibicao( usuarioExibicao );
            conversaRemetente.setIsGroup("false");
        }

        conversaRemetente.salvar();

    }

    private void recuperarMensagens(){

        listaMensagem.clear();

        childEventListenerMensagens = mensagemRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                listaMensagem.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void tirarFoto(View view){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ( i.resolveActivity(getPackageManager()) != null ){
            startActivityForResult(i, SELECAO_CAMERA );
        }
    }
}
