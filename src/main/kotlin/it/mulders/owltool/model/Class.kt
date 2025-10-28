package it.mulders.owltool.model

import java.util.Locale
import java.util.UUID

@ConsistentCopyVisibility
data class Class
    private constructor(
        val identifier: String,
        val namespace: String?,
        val name: String?,
        val children: Collection<Class> = emptySet(),
    ) {
        fun withChildren(children: Collection<Class>) = Class(identifier, namespace, name, children)

        fun isAnonymous(): Boolean = name == null

        companion object {
            private val locale = Locale.getDefault()

            fun of(
                namespace: String?,
                name: String?,
                children: Collection<Class> = emptySet(),
            ): Class =
                if (name == null) {
                    Class(UUID.randomUUID().toString(), null, null, children)
                } else {
                    Class(name.lowercase(locale), namespace, name, children)
                }
        }
    }
