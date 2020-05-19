package com.example.whatsapp2.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp2.R;
import com.example.whatsapp2.activity.ChatActivity;
import com.example.whatsapp2.activity.GrupoActivity;
import com.example.whatsapp2.adapter.ContatosAdapter;
import com.example.whatsapp2.config.ConfiguracaoFirebase;
import com.example.whatsapp2.helper.RecyclerItemClickListener;
import com.example.whatsapp2.helper.UsuarioFirebase;
import com.example.whatsapp2.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        //Configuracoes Iniciais
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar um adapter
        adapter = new ContatosAdapter(listaContatos, getActivity());

        //Configurar um recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContatos.setLayoutManager(layoutManager);
        recyclerViewListaContatos.setHasFixedSize(true);
        recyclerViewListaContatos.setAdapter(adapter);

        //Configurar o evento de click
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                List<Usuario> listaAtualizada = adapter.getContatos();
                                Usuario usuarioSelecionado = listaAtualizada.get(position);
                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();
                                if (cabecalho){
                                    Intent intent = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(intent);
                                }else{
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("chatContato", usuarioSelecionado);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            }
                        }
                )
        );

        //Define usuario com email vazio como grupo
        Usuario itemGroup = new Usuario();
        itemGroup.setNome("Novo Grupo");
        itemGroup.setEmail("");
        listaContatos.add(itemGroup);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
        listaContatos.clear();
    }

    public void pesquisarContatos(String texto){

        List<Usuario> listaContatoBusca = new ArrayList<>();
        for (Usuario usuario : listaContatos){

            String nome = usuario.getNome().toLowerCase();
            String email = usuario.getEmail().toLowerCase();

            if (nome.contains(texto) || email.contains(texto)){
                listaContatoBusca.add(usuario);
            }
        }

        adapter = new ContatosAdapter(listaContatoBusca, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarContatos(){
        adapter = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperarContatos(){
        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dados : dataSnapshot.getChildren()){

                    Usuario usuario = dados.getValue(Usuario.class);

                    if( ! usuarioAtual.getEmail().equals(usuario.getEmail()) ){
                        listaContatos.add(usuario);
                    }

                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
