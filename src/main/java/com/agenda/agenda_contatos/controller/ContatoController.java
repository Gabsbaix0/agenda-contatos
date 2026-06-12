package com.agenda.agenda_contatos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agenda.agenda_contatos.model.Contato;
import com.agenda.agenda_contatos.repository.ContatoRepository;

@Controller
@RequestMapping("/contatos")
public class ContatoController {

    @Autowired
    private ContatoRepository repository;

    // Listar todos
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("contatos", repository.findAll());
        return "index";
    }

    // Abrir formulário novo
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("contato", new Contato());
        return "form";
    }

    // Abrir formulário editar
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("contato", repository.findById(id).orElseThrow());
        return "form";
    }

    // Salvar (novo e editar)
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Contato contato) {
        repository.save(contato);
        return "redirect:/contatos";
    }

    // Deletar
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/contatos";
    }
}