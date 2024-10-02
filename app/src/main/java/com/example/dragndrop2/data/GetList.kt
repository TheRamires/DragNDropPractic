package com.example.dragndrop2.data

import com.example.dragndrop2.model.PersonUiModel
import java.util.concurrent.atomic.AtomicInteger

private val count = AtomicInteger(0)

fun loadPersonList1(): List<PersonUiModel> {
    return listOf(
        PersonUiModel("Sting", count.getAndIncrement()),
        PersonUiModel("Goldberg", count.getAndIncrement()),
        PersonUiModel("Bret Hart", count.getAndIncrement()),
        PersonUiModel("Hulk Hogan", count.getAndIncrement()),
        PersonUiModel("Scot Steiner", count.getAndIncrement()),
        PersonUiModel("Kevin Nash", count.getAndIncrement()),
        PersonUiModel("Lex Luger", count.getAndIncrement()),
        PersonUiModel("Scott Hall", count.getAndIncrement()),
        PersonUiModel("DDP", count.getAndIncrement()),

        PersonUiModel("Bam Bam Bigellow", count.getAndIncrement()),
        PersonUiModel("Konnan", count.getAndIncrement()),
        PersonUiModel("Macho Man Randy Savage", count.getAndIncrement()),
        PersonUiModel("Raven", count.getAndIncrement()),
        PersonUiModel("Curt Hennig", count.getAndIncrement()),
        PersonUiModel("Booker T", count.getAndIncrement()),
        PersonUiModel("Buff Bagwell", count.getAndIncrement()),
        PersonUiModel("Rick Steiner", count.getAndIncrement()),
        PersonUiModel("Dean Malenko", count.getAndIncrement()),
        PersonUiModel("Horace Hogan", count.getAndIncrement()),
        PersonUiModel("Chris Benoit", count.getAndIncrement()),
        PersonUiModel("Chris Kanyon", count.getAndIncrement()),
        PersonUiModel("Norman Smiley", count.getAndIncrement()),
        PersonUiModel("Perry Saturn", count.getAndIncrement()),
        PersonUiModel("The Cat", count.getAndIncrement()),
        PersonUiModel("Wrath", count.getAndIncrement()),
        PersonUiModel("Chris Jericho", count.getAndIncrement()),
        PersonUiModel("Bobby Duncum, Jr.", count.getAndIncrement()),
        PersonUiModel("Alex Wright", count.getAndIncrement()),
        PersonUiModel("Scott Norton", count.getAndIncrement()),
        PersonUiModel("Disco Inferno", count.getAndIncrement()),
        PersonUiModel("Kenny Kaos", count.getAndIncrement()),
        PersonUiModel("Arn Anderson", count.getAndIncrement()),
        PersonUiModel("Bobby Blaze", count.getAndIncrement()),
        PersonUiModel("Barry Windham", count.getAndIncrement()),
    )
}

fun loadPersonList2(): List<PersonUiModel> {
    return listOf(
        PersonUiModel("Rey Mysterio, Jr.", count.getAndIncrement()),
        PersonUiModel("La Parka", count.getAndIncrement()),
        PersonUiModel("Psychosis", count.getAndIncrement()),
        PersonUiModel("Billy Kidman", count.getAndIncrement()),
        PersonUiModel("Prince Iaukea", count.getAndIncrement()),
        PersonUiModel("Eddie Guerrero", count.getAndIncrement()),
        PersonUiModel("Juventud Guerrera", count.getAndIncrement()),
        PersonUiModel("Kaz Hayashi", count.getAndIncrement()),
        PersonUiModel("Chavo Guerrero, Jr.", count.getAndIncrement()),
    )
}

fun loadPersonList3(): List<PersonUiModel> {
    return listOf(
        PersonUiModel("Rey Mysterio, Jr.", count.getAndIncrement()),
        PersonUiModel("La Parka", count.getAndIncrement()),
        PersonUiModel("Psychosis", count.getAndIncrement()),
        PersonUiModel("Billy Kidman", count.getAndIncrement()),
        PersonUiModel("Prince Iaukea", count.getAndIncrement()),
        PersonUiModel("Eddie Guerrero", count.getAndIncrement()),
        PersonUiModel("Juventud Guerrera", count.getAndIncrement()),
        PersonUiModel("Kaz Hayashi", count.getAndIncrement()),
        PersonUiModel("Chavo Guerrero, Jr.", count.getAndIncrement()),
    )
}
