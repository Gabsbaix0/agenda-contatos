package com.agenda.agenda_contatos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agenda.agenda_contatos.model.Contato;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> {

    List<Contato> findByNomeContainingIgnoreCaseOrTelefoneContainingIgnoreCase(String nome, String telefone);
}