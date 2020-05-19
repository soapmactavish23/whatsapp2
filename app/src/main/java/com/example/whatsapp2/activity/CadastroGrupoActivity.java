package com.example.whatsapp2.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.whatsapp2.adapter.GrupoSelecionadoAdapter;
import com.example.whatsapp2.config.ConfiguracaoFirebase;
import com.example.whatsapp2.helper.UsuarioFirebase;
import com.example.whatsapp2.model.Grupo;
import com.example.whatsapp2.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp2.R;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView txtTotalParticipantes, txtNomeGrupo;
    private RecyclerView recyclerMembrosSelecionados;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private CircleImageView imgGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        //Configuracoes Iniciais
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Adicionar Nome");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = findViewById(R.id.fabCadastrarGrupo);
        txtTotalParticipantes = findViewById(R.id.txtTotParticipantesGrupo);
        txtNomeGrupo = findViewById(R.id.txtNomeGrupo);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosGrupo);
        imgGrupo = findViewById(R.id.imgGrupo);
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        grupo = new Grupo();

        if(getIntent().getExtras() != null){
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados.addAll(membros);
            txtTotalParticipantes.setText("Participantes: " + listaMembrosSelecionados.size());
        }

        //Configurar recyclerview para os membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        //Cadastro
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nomeGrupo = txtNomeGrupo.getText().toString();
                if(!nomeGrupo.isEmpty()){
                    listaMembrosSelecionados.add(UsuarioFirebase.getUsuarioLogado());
                    grupo.setMembros(listaMembrosSelecionados);
                    grupo.setNome(nomeGrupo);
                    grupo.salvar();

                    Intent intent = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                    intent.putExtra("chatGrupo", grupo);
                    startActivity(intent);

                }else{
                    Toast.makeText(
                            getApplicationContext(),
                            "Preencha o campo nome",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

    }

    public void adicionarFotoGrupo(View view){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
        if ( i.resolveActivity(getPackageManager()) != null ){
            startActivityForResult(i, SELECAO_GALERIA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;

            try {

                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada );

                if ( imagem != null ){

                    imgGrupo.setImageBitmap( imagem );

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();

                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            String url = uri.getResult().toString();
                            grupo.setFoto(url);
                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
