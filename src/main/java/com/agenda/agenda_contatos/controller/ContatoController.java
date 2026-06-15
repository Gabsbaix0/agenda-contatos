package com.agenda.agenda_contatos.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
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
        popularContatos(busca, model);
        return "lista";
    }

    // Busca incremental: devolve só o fragmento com os resultados
    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String busca, Model model) {
        popularContatos(busca, model);
        return "lista :: resultados";
    }

    private void popularContatos(String busca, Model model) {
        var contatos = (busca == null || busca.isBlank())
                ? repository.findAll()
                : repository.findByNomeContainingIgnoreCaseOrTelefoneContainingIgnoreCase(busca, busca);
        model.addAttribute("contatos", contatos);
        model.addAttribute("busca", busca);
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
    public String salvar(@Valid @ModelAttribute Contato contato, BindingResult result,
                         @RequestParam("imagem") MultipartFile imagem) throws IOException {
        if (result.hasErrors()) {
            return "form";
        }
        // Na edição, parte do registro existente para preservar a foto atual
        Contato alvo = contato.getId() == null
                ? contato
                : repository.findById(contato.getId()).orElse(contato);
        if (alvo != contato) {
            alvo.setNome(contato.getNome());
            alvo.setTelefone(contato.getTelefone());
            alvo.setEmail(contato.getEmail());
        }
        if (!imagem.isEmpty() && imagem.getContentType() != null
                && imagem.getContentType().startsWith("image/")) {
            alvo.setFoto(imagem.getBytes());
            alvo.setFotoTipo(imagem.getContentType());
        }
        repository.save(alvo);
        return "redirect:/contatos";
    }

    // Servir a foto de um contato
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        var contato = repository.findById(id);
        if (contato.isEmpty() || contato.get().getFoto() == null) {
            return ResponseEntity.notFound().build();
        }
        var c = contato.get();
        var tipo = c.getFotoTipo() != null ? c.getFotoTipo() : MediaType.IMAGE_JPEG_VALUE;
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(tipo)).body(c.getFoto());
    }

    // Deletar
    @PostMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/contatos";
    }
}