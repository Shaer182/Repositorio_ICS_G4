package Grupo4.EcoHarmonyParkBack.services;

import Grupo4.EcoHarmonyParkBack.dtos.HorarioResponse;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import java.time.format.DateTimeFormatter;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void enviarConfirmacionInscripcion(String para, InscripcionResponse inscripcion) {
        try {
            HorarioResponse horario = inscripcion.getHorario();
            String fecha = horario.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaInicio = horario.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm"));
            String horaFin = horario.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm"));

            // Crear el contexto con las variables
            Context context = new Context();
            context.setVariable("actividad", horario.getNombreActividad());
            context.setVariable("fecha", fecha);
            context.setVariable("horaInicio", horaInicio);
            context.setVariable("horaFin", horaFin);
            context.setVariable("cantidadPersonas", inscripcion.getCantidadPersonas());
            context.setVariable("visitantes", inscripcion.getVisitantes());

            // Procesar el HTML del template
            String htmlContent = templateEngine.process("email/confirmacion-inscripcion.html", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(para);
            helper.setSubject("🌿 Confirmación de Inscripción - EcoHarmony Park");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            System.out.println("Correo de confirmación enviado a: " + para);

        } catch (MessagingException e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
        }
    }
}
