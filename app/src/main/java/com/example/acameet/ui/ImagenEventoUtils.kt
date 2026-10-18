
package com.example.acameet.ui

import java.text.Normalizer
import java.util.Locale

object ImagenEventoUtils {

    private fun normalizar(texto: String): String {
        return Normalizer.normalize(
            texto.lowercase(Locale.ROOT),
            Normalizer.Form.NFD
        ).replace("\\p{Mn}+".toRegex(), "")
    }

    fun obtenerImagen(
        titulo: String,
        categoria: String
    ): String {

        val tema = normalizar("$titulo $categoria")

        return when {

            // INTELIGENCIA ARTIFICIAL Y ROBÓTICA
            listOf(
                "inteligencia artificial",
                "machine learning",
                "robotica",
                "robot",
                "automatizacion",
                "ia generativa"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=900"

            // CIBERSEGURIDAD
            listOf(
                "ciberseguridad",
                "seguridad informatica",
                "hacking",
                "hackathon",
                "seguridad digital"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=900"

            // PROGRAMACIÓN Y SOFTWARE
            listOf(
                "programacion",
                "software",
                "desarrollo web",
                "aplicaciones moviles",
                "informatica",
                "codigo",
                "desarrollo de aplicaciones"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=900"

            // MEDIO AMBIENTE
            listOf(
                "medio ambiente",
                "ecologia",
                "reciclaje",
                "sostenibilidad",
                "ambiental",
                "naturaleza"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=900"

            // EMPRENDIMIENTO Y NEGOCIOS
            listOf(
                "emprendimiento",
                "negocios",
                "marketing",
                "empresarial",
                "empresa",
                "innovacion"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1521737711867-e3b97375f902?w=900"

            // MEDICINA Y SALUD
            listOf(
                "medicina",
                "enfermeria",
                "salud",
                "primeros auxilios",
                "hospital"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=900"

            // EDUCACIÓN E INVESTIGACIÓN
            listOf(
                "educacion",
                "pedagogia",
                "investigacion",
                "academico",
                "seminario",
                "estudiantes",
                "universidad"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=900"

            // TECNOLOGÍA EN GENERAL
            listOf(
                "tecnologia",
                "digital",
                "computacion",
                "electronica"
            ).any { tema.contains(it) } ->
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=900"

            // IMAGEN GENERAL PARA OTROS EVENTOS
            else ->
                "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=900"
        }
    }
}
