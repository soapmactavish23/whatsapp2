package com.example.whatsapp2.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp2.R;
import com.example.whatsapp2.helper.UsuarioFirebase;
import com.example.whatsapp2.model.Mensagem;

import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private List<Mensagem> listaMensagem;
    private Context context;
    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;

    public MensagensAdapter(List<Mensagem> listaMensagem, Context context) {
        this.listaMensagem = listaMensagem;
        this.context = context;
    }

    //Monta o layout
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        if(viewType == TIPO_REMETENTE){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);
        }else if(viewType == TIPO_DESTINATARIO){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario,parent,false);
        }
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = listaMensagem.get(position);
        String msg = mensagem.getMensagem();
        String img = mensagem.getImagem();

        if(img != null){
            Uri url = Uri.parse(img);
            Glide.with(context).load(url).into(holder.imagem);

            String nome = mensagem.getNome();
            if(!nome.isEmpty()){
                holder.nome.setText(nome);
            }else{
                holder.nome.setVisibility(View.GONE);
            }

            //Esconder o texto
            holder.mensagem.setVisibility(View.GONE);

        }else{
            holder.mensagem.setText(msg);

            String nome = mensagem.getNome();
            if(!nome.isEmpty()){
                holder.nome.setText(nome);
            }else{
                holder.nome.setVisibility(View.GONE);
            }

            //Esconder a imagem
            holder.imagem.setVisibility(View.GONE);
        }

    }

    //Conta os itens
    @Override
    public int getItemCount() {
        return listaMensagem.size();
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = listaMensagem.get(position);

        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();

        if(idUsuario.equals( mensagem.getIdUsuario() )){
            return TIPO_REMETENTE;
        }else{
            return TIPO_DESTINATARIO;
        }

    }

    //Linka com os itens
    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mensagem, nome;
        ImageView imagem;

        public MyViewHolder(View itemView){
            super(itemView);

            nome = itemView.findViewById(R.id.txtNomeExibicao);
            mensagem = itemView.findViewById(R.id.txtTexto);
            imagem = itemView.findViewById(R.id.imgFoto);

        }
    }

}
