package com.example.whatsapp2.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp2.R;
import com.example.whatsapp2.activity.ChatActivity;
import com.example.whatsapp2.adapter.ConversasAdapter;
import com.example.whatsapp2.config.ConfiguracaoFirebase;
import com.example.whatsapp2.helper.RecyclerItemClickListener;
import com.example.whatsapp2.helper.UsuarioFirebase;
import com.example.whatsapp2.model.Conversa;
import com.example.whatsapp2.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private List<Conversa> listaConversa = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference databaseReference;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewConversas = view.findViewById(R.id.recyclerListaConversas);

        //Configurar adapter
        adapter = new ConversasAdapter(listaConversa, getActivity());

        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter(adapter);

        //Configurar evento de clique
        recyclerViewConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                List<Conversa> listaConversaAtualizada = adapter.getConversa();
                                Conversa conversaSelecionada = listaConversaAtualizada.get(position);
                                if(conversaSelecionada.getIsGroup().equals("true")){
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(intent);
                                }else{
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
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

        //Configurar conversas ref
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();
        databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = databaseReference.child("conversa").child(idUsuario);

        listaConversa.clear();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
        listaConversa.clear();
    }

    public void pesquisarConversas(String texto){

        List<Conversa> listaConversaBusca = new ArrayList<>();
        for (Conversa conversa : listaConversa){

            if(conversa.getUsuarioExibicao() != null){
                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if (nome.contains(texto) || ultimaMsg.contains(texto)){
                    listaConversaBusca.add(conversa);
                }
            }else{
                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if (nome.contains(texto) || ultimaMsg.contains(texto)){
                    listaConversaBusca.add(conversa);
                }
            }
        }

        adapter = new ConversasAdapter(listaConversaBusca, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarConversas(){
        adapter = new ConversasAdapter(listaConversa, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperarConversas(){
        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Recuperar conversas
                Conversa conversa = dataSnapshot.getValue(Conversa.class);
                listaConversa.add(conversa);
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
}
