package com.agenda.agenda_contatos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agenda.agenda_contatos.model.Contato;
import com.agenda.agenda_contatos.repository.ContatoRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/contatos")
public class ContatoController {

    @Autowired
    private ContatoRepository repository;

    // Listar todos (ou filtrar por nome/telefone)
    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        var contatos = (busca == null || busca.isBlank())
                ? repository.findAll()
                : repository.findByNomeContainingIgnoreCaseOrTelefoneContainingIgnoreCase(busca, busca);
        model.addAttribute("contatos", contatos);
        model.addAttribute("busca", busca);
        return "lista";
    }

    // Abrir formulário novo
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("contato", new Contato());
        return "form";
    }

    // Abrir formulário editar
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var contato = repository.findById(id);
        if (contato.isEmpty()) {
            ra.addFlashAttribute("mensagem", "Contato não encontrado.");
            return "redirect:/contatos";
        }
        model.addAttribute("contato", contato.get());
        return "form";
    }

    // Salvar (novo e editar)
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Contato contato, BindingResult result) {
        if (result.hasErrors()) {
            return "form";
        }
        repository.save(contato);
        return "redirect:/contatos";
    }

    // Deletar
    @PostMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/contatos";
    }
}