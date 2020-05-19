package com.example.whatsapp2.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp2.R;
import com.example.whatsapp2.model.Conversa;
import com.example.whatsapp2.model.Grupo;
import com.example.whatsapp2.model.Usuario;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {

    private List<Conversa> conversaList;
    private Context context;

    public ConversasAdapter(List<Conversa> conversaList, Context context) {
        this.conversaList = conversaList;
        this.context = context;
    }

    public List<Conversa> getConversa(){
        return conversaList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversa conversa = conversaList.get(position);
        holder.ultimaMensagem.setText(conversa.getUltimaMensagem());

        if(conversa.getIsGroup().equals("true")){

            Grupo grupo = conversa.getGrupo();
            holder.nome.setText(grupo.getNome());

            if (grupo.getFoto() != null){
                Uri uri = Uri.parse(grupo.getFoto());
                Glide.with(context).load(uri).into(holder.foto);
            }else{
                holder.foto.setImageResource(R.drawable.padrao);
            }

        }else{
            Usuario usuario = conversa.getUsuarioExibicao();
            if(usuario != null){
                holder.nome.setText(usuario.getNome());

                if (usuario.getFoto() != null){
                    Uri uri = Uri.parse(usuario.getFoto());
                    Glide.with(context).load(uri).into(holder.foto);
                }else{
                    holder.foto.setImageResource(R.drawable.padrao);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return conversaList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome, ultimaMensagem;

        public MyViewHolder(View itemView){
            super(itemView);

            foto = itemView.findViewById(R.id.imgFotoContato);
            nome = itemView.findViewById(R.id.txtNomeContato);
            ultimaMensagem = itemView.findViewById(R.id.txtEmailContato);

        }
    }

}
