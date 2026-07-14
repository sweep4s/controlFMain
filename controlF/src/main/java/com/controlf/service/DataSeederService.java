package com.controlf.service;

import com.controlf.db.repository.*;
import com.controlf.db.schema.*;
import com.controlf.db.schema.enums.EstadoLey;
import com.controlf.db.schema.enums.ImpactoEsperado;
import com.controlf.db.schema.enums.NivelCoherencia;
import com.controlf.db.schema.enums.TipoVoto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSeederService {

    private final PoliticoRepository politicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LeyRepository leyRepository;
    private final VotoRepository votoRepository;
    private final PromesaRepository promesaRepository;
    private final VinculoPromesaLeyRepository vinculoRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final ComentarioRepository comentarioRepository;
    private final CalificacionRepository calificacionRepository;

    @Transactional
    public void seed() {
        if (politicoRepository.count() > 0) return;

        // 1. CONFIGURACION
        configuracionRepository.saveAll(Arrays.asList(
            new Configuracion("UMBRAL_COHERENCIA_ALTA", "70", "Límite para ser Coherente"),
            new Configuracion("UMBRAL_COHERENCIA_MEDIA", "40", "Límite para ser Ambiguo")
        ));

        // 2. USUARIOS
        Usuario u1 = new Usuario(null, "Admin Auditor", "admin@controlf.ec", "hash", "https://i.pravatar.cc/150?u=admin", Usuario.Rol.ADMIN, LocalDateTime.now(), null, null);
        Usuario u2 = new Usuario(null, "Juan Pérez", "juan.perez@ecuador.com", "hash", "https://i.pravatar.cc/150?u=juan", Usuario.Rol.CIUDADANO, LocalDateTime.now(), null, null);
        Usuario u3 = new Usuario(null, "Maria López", "maria.lopez@veeduria.ec", "hash", "https://i.pravatar.cc/150?u=maria", Usuario.Rol.CIUDADANO, LocalDateTime.now(), null, null);
        usuarioRepository.saveAll(Arrays.asList(u1, u2, u3));

        // 3. POLITICOS
        Politico p1 = new Politico(null, "Daniel Noboa Azín", "ADN", "Presidente de la República", "Guayas", "Ejecutivo", true, new BigDecimal("600000"), 
            "Empresario. Presidente más joven de la historia de Ecuador.", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Noboa_en_24_mayo_de_2026.jpg/960px-Noboa_en_24_mayo_de_2026.jpg", null, null, null, null);
        
        Politico p2 = new Politico(null, "Henry Kronfle", "PSC", "Ex-Presidente Asamblea", "Guayas", "Legislativo", false, new BigDecimal("450000"), 
            "Empresario guayaquileño. Candidato presidencial 2025.", "https://upload.wikimedia.org/wikipedia/commons/f/fa/Henry_Kronfle_2024.jpg", null, null, null, null);
        
        Politico p3 = new Politico(null, "Viviana Veloz", "RC5", "Presidenta Asamblea", "Santo Domingo", "Fiscalización", true, new BigDecimal("85000"), 
            "Líder del bloque correísta. Primera mujer de Santo Domingo en presidir la Asamblea.", "https://upload.wikimedia.org/wikipedia/commons/6/65/Viviana_Veloz_en_diciembre_de_2024.jpeg", null, null, null, null);
        
        Politico p4 = new Politico(null, "Pabel Muñoz", "RC5", "Alcalde de Quito", "Pichincha", "Administración Local", true, new BigDecimal("120000"), 
            "Sociólogo. Ex-Secretario de Planificación.", "https://upload.wikimedia.org/wikipedia/commons/e/ef/Pabel_Mu%C3%B1oz_2022.jpg", null, null, null, null);
        
        Politico p5 = new Politico(null, "Jan Topić", "SUMA", "Candidato Presidencial", "Guayas", "Seguridad", true, new BigDecimal("900000"), 
            "Empresario en seguridad y tecnología. Ex-combatiente.", "https://upload.wikimedia.org/wikipedia/commons/5/5b/FOTOS_PERFIL-03_JAN_TOPIC.png", null, null, null, null);
        
        Politico p6 = new Politico(null, "Luisa González", "RC5", "Presidenta RC5", "Manabí", "Política Nacional", true, new BigDecimal("110000"), 
            "Abogada. Candidata presidencial 2023 y 2025.", "https://live.staticflickr.com/65535/52340671842_9664a76f47_o.jpg", null, null, null, null);
 //TEST
        Politico p7 = new Politico(null, "MARÍA CRISTINA ACUÑA VACA", "INDEPENDIENTE", "Asambleísta", "Pastaza", "Legislativo", true, new BigDecimal("85000"), "Asambleísta Nacional.", null, null, null, null, null);

       Politico p8 = new Politico(null, "ALEJANDRO LORENZO VANEGAS CORTÁZAR", "INDEPENDIENTE", "Asambleísta", "Guayas", "Legislativo", true, new BigDecimal("85000"), "Asambleísta Nacional.", null, null, null, null, null);
           

        politicoRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6 ,p7, p8));

        // 4. LEYES
        // Orden del constructor @AllArgsConstructor de Ley:
        // (id, titulo, codigo, tipoExpediente, proponente, descripcionOriginal,
        //  descripcionSimplificada, impactoSocial, categoria, estado, fechaIngreso,
        //  externalId, votos, comentarios, calificaciones, vinculos)
        List<Ley> leyes = new ArrayList<>();
        leyes.add(new Ley(null, "Ley para el Conflicto Armado Interno", "LEY-IVA-15", "Urgente Económico", "Daniel Noboa", "Incremento del IVA del 12% al 15% para financiar la guerra contra el crimen.", null, "Aumento del costo de la canasta básica.", "Economía", EstadoLey.APROBADA, LocalDate.of(2024, 2, 1), null, null, null, null, null));
        leyes.add(new Ley(null, "Ley de Competitividad Energética", "LEY-ENER-01", "Urgente Económico", "Daniel Noboa", "Promueve inversión privada en generación eléctrica para evitar apagones.", null, "Incentiva proyectos solares y eólicos.", "Energía", EstadoLey.APROBADA, LocalDate.of(2024, 1, 10), null, null, null, null, null));
        leyes.add(new Ley(null, "Aplicación Consulta Popular 2024", "LEY-CONSULTA", "Ordinario", "Asamblea Nacional", "Incremento drástico de penas para terrorismo y sicariato.", null, "Endurecimiento del sistema carcelario.", "Seguridad", EstadoLey.APROBADA, LocalDate.of(2024, 7, 15), null, null, null, null, null));
        leyes.add(new Ley(null, "Ley de Fortalecimiento Turístico", "LEY-TUR-01", "Urgente Económico", "Daniel Noboa", "Reducción de impuestos para eventos y turismo.", null, "Incentiva el turismo nacional.", "Turismo", EstadoLey.APROBADA, LocalDate.of(2024, 3, 25), null, null, null, null, null));
        leyes.add(new Ley(null, "Ley de Extradición y Seguridad", "LEY-TRA-01", "Ordinario", "Asamblea Nacional", "Reformas para permitir la extradición de ecuatorianos vinculados al narco.", null, "Herramienta contra el narcotráfico.", "Seguridad", EstadoLey.APROBADA, LocalDate.of(2024, 5, 10), null, null, null, null, null));

        leyRepository.saveAll(leyes);

        // 5. PROMESAS
        List<Promesa> promesas = new ArrayList<>();
        promesas.add(new Promesa(null, "No subiré impuestos a la clase media ni sectores populares.", "Economía", LocalDate.of(2023, 10, 1), p1, null));
        promesas.add(new Promesa(null, "Plan Fénix: Control total de las cárceles y seguridad.", "Seguridad", LocalDate.of(2023, 9, 15), p1, null));
        promesas.add(new Promesa(null, "Extradición inmediata para criminales peligrosos.", "Seguridad", LocalDate.of(2023, 8, 15), p5, null));
        promesas.add(new Promesa(null, "Protección de cada hectárea de nuestra Amazonía.", "Medio Ambiente", LocalDate.of(2023, 9, 10), p6, null));
        promesaRepository.saveAll(promesas);

        // 6. VOTOS (Matriz masiva)
        // La fecha de cada voto es la de ingreso de su ley (la votación ocurre en esa
        // sesión), no "ahora": así la serie mensual refleja las fechas reales y no se
        // concentra todo en el mes actual.
        votoRepository.save(new Voto(null, TipoVoto.FAVOR, true, leyes.get(0).getFechaIngreso().atStartOfDay(), p1, leyes.get(0)));
        votoRepository.save(new Voto(null, TipoVoto.FAVOR, true, leyes.get(1).getFechaIngreso().atStartOfDay(), p1, leyes.get(1)));
        votoRepository.save(new Voto(null, TipoVoto.FAVOR, true, leyes.get(3).getFechaIngreso().atStartOfDay(), p1, leyes.get(3)));
        votoRepository.save(new Voto(null, TipoVoto.CONTRA, true, leyes.get(0).getFechaIngreso().atStartOfDay(), p3, leyes.get(0)));
        votoRepository.save(new Voto(null, TipoVoto.FAVOR, true, leyes.get(1).getFechaIngreso().atStartOfDay(), p3, leyes.get(1)));
        votoRepository.save(new Voto(null, TipoVoto.CONTRA, true, leyes.get(4).getFechaIngreso().atStartOfDay(), p3, leyes.get(4)));
        votoRepository.save(new Voto(null, TipoVoto.FAVOR, true, leyes.get(0).getFechaIngreso().atStartOfDay(), p5, leyes.get(0)));
        votoRepository.save(new Voto(null, TipoVoto.FAVOR, true, leyes.get(4).getFechaIngreso().atStartOfDay(), p5, leyes.get(4)));

        // 7. VINCULOS
        vinculoRepository.save(new VinculoPromesaLey(null, ImpactoEsperado.NEGATIVO, NivelCoherencia.INCUMPLE, "Aprobó el alza del IVA al 15% pese a prometer repetidamente en campaña que no subiría impuestos.", promesas.get(0), leyes.get(0)));
        vinculoRepository.save(new VinculoPromesaLey(null, ImpactoEsperado.POSITIVO, NivelCoherencia.CUMPLE, "Votó a favor de la extradición tal como prometió en su plan de mano dura.", promesas.get(2), leyes.get(4)));

        // 8. SOCIAL
        Comentario com = new Comentario(null, "El IVA nos está matando el bolsillo, Noboa mintió.", true, LocalDateTime.now(), u2);
        comentarioRepository.save(com);
        p1.setComentarios(new ArrayList<>(List.of(com)));
        politicoRepository.save(p1);
    }
}
