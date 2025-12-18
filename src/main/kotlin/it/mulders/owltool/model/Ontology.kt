package it.mulders.owltool.model

data class Ontology(
    private val classes: Collection<Class>,
) {
    fun classCount(): Int = classes.countRecursively()

    private fun Collection<Class>.countRecursively(): Int = this.asSequence().map { it.children.countRecursively() + 1 }.sum()

    fun rootClasses(): Collection<Class> = classes

    fun allClasses(): Collection<Class> {
        val result = mutableSetOf<Class>()

        fun collect(classes: Collection<Class>) {
            for (cls in classes) {
                result.add(cls)
                collect(cls.children)
            }
        }
        collect(this.classes)
        return result
    }
}
