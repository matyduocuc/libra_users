package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.BookDao
import com.empresa.libra_users.data.local.user.BookEntity

class BookRepository(private val bookDao: BookDao) {
    suspend fun insert(book: BookEntity): Long = bookDao.insert(book)
    suspend fun getAllBooks(): List<BookEntity> = bookDao.getAllBooks()
    suspend fun update(book: BookEntity) = bookDao.update(book)
    suspend fun delete(book: BookEntity) = bookDao.delete(book)
    suspend fun count(): Int = bookDao.count()

    // Función corregida para buscar el libro por ID en la lista hardcodeada
    suspend fun getBookById(id: Long): BookEntity? {
        return getCategorizedBooks().values.flatten().find { it.id == id }
    }

    suspend fun getCategorizedBooks(): Map<String, List<BookEntity>> {
        // Datos del JSON proporcionado por el usuario
        return mapOf(
            "Clásicos universales" to listOf(
                BookEntity(id = 1, title = "1984", author = "George Orwell", isbn = " George Orwell, cuyo verdadero nombre era Eric Arthur Blair, fue un escritor y periodista británico nacido en 1903 en la India y fallecido en 1950 en Londres. Fue un crítico del totalitarismo, la manipulación política y la pérdida de la libertad individual. Su estilo es directo, lúcido y comprometido con la justicia social.\n" +
                        "\n" +
                        "Sobre el libro “1984”:\n" +
                        "Publicada en 1949, “1984” es una novela distópica que describe un mundo controlado por un régimen autoritario que vigila cada pensamiento de sus ciudadanos. La obra introduce conceptos como el Gran Hermano, la neolengua y el doblepensar. Es una advertencia sobre los peligros de la opresión política y la manipulación de la verdad.", coverUrl = "https://books.google.cl/books/content?id=yxv1LK5gyV4C&printsec=frontcover&img=1&zoom=1&imgtk=AFLRE709ApWTwmDGcaEhgVM4kCSB3tbQ8lPS_YcHkQt2rJfr8baeRKvcz0sfK_ZbHCwnvHVI5f2NzbPQqemPhT78pIJk1UeG3IV30GdzMvNYquT1VglrvYIcd9CNGO51NkgJsw6gM1kF", categoryId = 1, publisher = "Debolsillo", publishDate = "1949-06-08", status = "Available", inventoryCode = "C001"),
                BookEntity(id = 2, title = "El Principito", author = "Antoine de Saint-Exupéry", isbn = "Antoine de Saint-Exupéry (1900–1944) fue un aviador y escritor francés. Su obra combina experiencias personales con reflexiones filosóficas sobre la humanidad, la amistad y el amor.\n" +
                        "\n" +
                        "Sobre el libro: “El Principito” es una fábula poética sobre un niño que viaja por distintos planetas y descubre lo esencial de la vida. Es una metáfora sobre la pureza del corazón frente a la indiferencia del mundo adulto.", coverUrl = "https://m.media-amazon.com/images/I/713nEf55PkL._SL1500_.jpg", categoryId = 1, publisher = "Salamandra", publishDate = "1943-04-06", status = "Available", inventoryCode = "C002"),
                BookEntity(id = 3, title = "Orgullo y prejuicio", author = "Jane Austen", isbn = "Jane Austen (1775–1817) fue una novelista inglesa que retrató con ironía y sutileza las costumbres y jerarquías sociales de su tiempo. Es pionera de la novela romántica moderna.\n" +
                        "\n" +
                        "Sobre el libro: Publicada en 1813, esta historia sigue a Elizabeth Bennet y el señor Darcy en una trama de orgullo, malentendidos y redención. Una crítica elegante a los prejuicios sociales y de clase.", coverUrl = "https://tse2.mm.bing.net/th/id/OIP.rVCEEI0KvromX4U9hDdnVwHaLJ?rs=1&pid=ImgDetMain&o=7&rm=3", categoryId = 1, publisher = "Penguin Clásicos", publishDate = "1813-01-28", status = "Available", inventoryCode = "C003"),
                BookEntity(id = 4, title = "Moby-Dick", author = "Herman Melville", isbn = "Herman Melville (1819–1891) fue un novelista y marinero estadounidense. Su vida en el mar inspiró gran parte de su obra, caracterizada por la profundidad filosófica y simbólica.\n" +
                        "\n" +
                        "Sobre el libro: “Moby-Dick” (1851) narra la obsesiva cacería del capitán Ahab contra una ballena blanca. Es una alegoría de la lucha del hombre contra el destino, la naturaleza y sus propios límites.", coverUrl = "https://www.planetalibro.net/biblioteca/m/e/melville/melville-herman-moby-dick/melville-herman-moby-dick.jpg", categoryId = 1, publisher = "Alma", publishDate = "1851-10-18", status = "Available", inventoryCode = "C004"),
                BookEntity(id = 5, title = "Cien años de soledad", author = "Gabriel García Márquez", isbn = "Gabriel García Márquez (1927–2014) fue un escritor colombiano y ganador del Premio Nobel de Literatura en 1982. Es el máximo exponente del realismo mágico.\n" +
                        "\n" +
                        "Sobre el libro: Publicada en 1967, la novela cuenta la historia de la familia Buendía en Macondo, un pueblo donde lo fantástico y lo cotidiano se entrelazan. Es una epopeya sobre la soledad, el tiempo y la historia latinoamericana.", coverUrl = "https://th.bing.com/th/id/R.96daf80eb401e6eca4c96e5c6a2ab7ac?rik=HpeztcqjJWyrzw&pid=ImgRaw&r=0", categoryId = 1, publisher = "Diana", publishDate = "1967-05-30", status = "Loaned", inventoryCode = "C005")
            ),
            "Ciencia ficción y fantasía" to listOf(
                BookEntity(id = 6, title = "Dune", author = "Frank Herbert", isbn = "Frank Herbert\n" +
                        "Sobre el autor: Frank Herbert (1920–1986) fue un escritor estadounidense de ciencia ficción que exploró temas de ecología, religión y poder político.\n" +
                        "\n" +
                        "Sobre el libro: “Dune” (1965) sigue a Paul Atreides en un planeta desértico llamado Arrakis, fuente de la especia más valiosa del universo. Es una obra sobre política, profecía y supervivencia.", coverUrl = "https://1.bp.blogspot.com/-xO-f2oZ5ZJ0/Xux2y28OJZI/AAAAAAAAZMA/AsHZHkdR8OIYK6mHcACR-GcU3qkyjRvPACNcBGAsYHQ/s1600/dune.jpg", categoryId = 2, publisher = "Debolsillo", publishDate = "1965-08-01", status = "Available", inventoryCode = "SF001"),
                BookEntity(id = 7, title = "El Hobbit", author = "J.R.R. Tolkien", isbn = "John Ronald Reuel Tolkien (1892–1973) fue filólogo, profesor de Oxford y creador de la mitología moderna. Su imaginación dio forma a la Tierra Media.\n" +
                        "\n" +
                        "Sobre el libro: “El Hobbit” (1937) relata la aventura de Bilbo Bolsón, un hobbit que se une a un grupo de enanos para recuperar un tesoro custodiado por el dragón Smaug. Una historia de coraje y crecimiento.", coverUrl = "https://www.raccoongames.es/img/productos/2022/04/02/portada_el-hobbit-ne_j-r-r-tolkien_202202140958.jpeg", categoryId = 2, publisher = "Minotauro", publishDate = "1937-09-21", status = "Available", inventoryCode = "SF002"),
                BookEntity(id = 8, title = "Harry Potter y la piedra filosofal", author = "J.K. Rowling", isbn = "Joanne Rowling (1965) es una escritora británica creadora de la saga de Harry Potter. Su trabajo mezcla fantasía, amistad y temas de identidad y sacrificio.\n" +
                        "\n" +
                        "Sobre el libro: “Harry Potter y la piedra filosofal” (1997) presenta a Harry, un niño que descubre que es mago. En Hogwarts encontrará amigos, misterios y su destino frente a Lord Voldemort.", coverUrl = "https://i0.wp.com/www.epubgratis.org/wp-content/uploads/2012/04/Harry-Potter-y-la-Piedra-Filosofal-J.K.-Rowling-portada.jpg?fit=683%2C1024&ssl=1", categoryId = 2, publisher = "Salamandra", publishDate = "1997-06-26", status = "Available", inventoryCode = "SF003"),
                BookEntity(id = 9, title = "Neuromante", author = "William Gibson", isbn = "William Gibson (1948) es un escritor canadiense-estadounidense considerado el padre del ciberpunk. Su obra anticipó Internet y la realidad virtual.\n" +
                        "\n" +
                        "Sobre el libro: “Neuromante” (1984) sigue a un hacker que es contratado para una peligrosa misión digital. Es una novela pionera en la visión de un futuro dominado por corporaciones y tecnología.", coverUrl = "https://tse1.mm.bing.net/th/id/OIP.EWv-kvt7VepbXpJWrVX2jwHaLQ?rs=1&pid=ImgDetMain&o=7&rm=3", categoryId = 2, publisher = "Minotauro", publishDate = "1984-07-01", status = "Damaged", inventoryCode = "SF004"),
                BookEntity(id = 10, title = "Fundación", author = "Isaac Asimov", isbn = "Isaac Asimov (1920–1992) fue un bioquímico y prolífico escritor ruso-estadounidense, autor de más de 500 libros de ciencia y ficción.\n" +
                        "\n" +
                        "Sobre el libro: “Fundación” (1951) inicia la legendaria saga del Imperio Galáctico. Narra cómo Hari Seldon crea una institución para preservar el conocimiento ante la inevitable caída del Imperio.", coverUrl = "https://1.bp.blogspot.com/-VNj464HCl00/WW5b5Oqhz2I/AAAAAAAAGHE/tQuUsLgj3aA7oL0gz_60vYN9yYgBPIlaACKgBGAs/s1600/fundacion-libro-isaac-asimov.JPG", categoryId = 2, publisher = "Debolsillo", publishDate = "1951-06-01", status = "Available", inventoryCode = "SF005")
            ),
            "Romance y drama" to listOf(
                BookEntity(id = 11, title = "Bajo la misma estrella", author = "John Green", isbn = "John Green (1977) es un autor y youtuber estadounidense. Es conocido por sus historias realistas sobre la juventud, el amor y la mortalidad.\n" +
                        "\n" +
                        "Sobre el libro: “Bajo la misma estrella” (2012) sigue a Hazel y Gus, dos adolescentes con cáncer que viven un amor intenso mientras enfrentan la fragilidad de la vida.", coverUrl = "https://th.bing.com/th/id/R.e134c57e2b93d7ac6cd12d355241fbd0?rik=utPtyzXJG3v7Kg&riu=http%3a%2f%2f3.bp.blogspot.com%2f-Dmyq00jXSz0%2fUlVaaMi6NwI%2fAAAAAAAAAq8%2f_J-CmEj4hYs%2fs1600%2fPortada%2bBajo%2bla%2bmisma%2bestrella.jpg&ehk=mDlhTmrwbB8Zt51I0DZ9tDk40XGsiMyRXdI%2fwNRUj0Y%3d&risl=&pid=ImgRaw&r=0", categoryId = 3, publisher = "Nube de Tinta", publishDate = "2012-01-10", status = "Available", inventoryCode = "RD001"),
                BookEntity(id = 12, title = "Romeo y Julieta", author = "William Shakespeare", isbn = "William Shakespeare (1564–1616) fue dramaturgo, poeta y actor inglés, considerado el escritor más influyente de la lengua inglesa.\n" +
                        "\n" +
                        "Sobre el libro: Escrita hacia 1597, esta tragedia cuenta el amor imposible entre dos jóvenes de familias rivales. Es la historia más icónica sobre el destino y el sacrificio por amor.", coverUrl = "https://th.bing.com/th/id/OIP.fIWSKNeoWmumrY9se0TFzAHaLu?w=198&h=314&c=7&r=0&o=7&pid=1.7&rm=3", categoryId = 3, publisher = "Austral", publishDate = "1597-01-01", status = "Available", inventoryCode = "RD002"),
                BookEntity(id = 13, title = "Lo que el viento se llevó", author = "Margaret Mitchell", isbn = "Margaret Mitchell (1900–1949) fue una escritora estadounidense cuya única novela publicada la convirtió en un fenómeno mundial.\n" +
                        "\n" +
                        "Sobre el libro: “Lo que el viento se llevó” (1936) sigue la vida de Scarlett O’Hara durante la Guerra Civil estadounidense. Una historia de supervivencia, orgullo y amor imposible.", coverUrl = "https://tse4.mm.bing.net/th/id/OIP.hZqqeJCVUVYLM7xvuNx79AHaLT?rs=1&pid=ImgDetMain&o=7&rm=3", categoryId = 3, publisher = "Ediciones B", publishDate = "1936-06-30", status = "Retired", inventoryCode = "RD003"),
                BookEntity(id = 14, title = "Jane Eyre", author = "Charlotte Brontë", isbn = "Charlotte Brontë (1816–1855) fue una novelista inglesa que abordó la independencia femenina y las limitaciones sociales de su tiempo.\n" +
                        "\n" +
                        "Sobre el libro: “Jane Eyre” (1847) narra la vida de una huérfana que lucha por su dignidad y amor propio mientras descubre oscuros secretos en la mansión Thornfield.", coverUrl = "https://images.cdn3.buscalibre.com/fit-in/360x360/9b/4d/9b4d0c92dd7e99db4cfafad1cc33c5a8.jpg", categoryId = 3, publisher = "Alba", publishDate = "1847-10-16", status = "Available", inventoryCode = "RD004"),
                BookEntity(id = 15, title = "Cometas en el cielo", author = "Khaled Hosseini", isbn = "Khaled Hosseini (1965) es un escritor afgano-estadounidense que combina historia, emoción y crítica social en sus obras.\n" +
                        "\n" +
                        "Sobre el libro: “Cometas en el cielo” (2003) trata sobre la amistad y la redención entre dos niños afganos marcados por la traición y el exilio.", coverUrl = "https://m.media-amazon.com/images/I/81qKWjnjayL._SL1500_.jpg", categoryId = 3, publisher = "Salamandra", publishDate = "2003-05-29", status = "Available", inventoryCode = "RD005")
            ),
            "Misterio y suspenso" to listOf(
                BookEntity(id = 16, title = "El código Da Vinci", author = "Dan Brown", isbn = "Dan Brown (1964) es un novelista estadounidense especializado en thrillers que mezclan arte, religión y símbolos ocultos.\n" +
                        "\n" +
                        "Sobre el libro: “El código Da Vinci” (2003) sigue al profesor Robert Langdon en una carrera para descubrir un secreto milenario oculto por la Iglesia y el Priorato de Sion.", coverUrl = "https://tse2.mm.bing.net/th/id/OIP.sIkdDAiKEwynPBn-jjhVEQHaLH?rs=1&pid=ImgDetMain&o=7&rm=3", categoryId = 4, publisher = "Planeta", publishDate = "2003-03-18", status = "Available", inventoryCode = "MS001"),
                BookEntity(id = 17, title = "Asesinato en el Orient Express", author = "Agatha Christie", isbn = "Agatha Christie (1890–1976) fue una escritora británica y la reina del misterio, creadora de detectives como Hércules Poirot y Miss Marple.\n" +
                        "\n" +
                        "Sobre el libro: En “Asesinato en el Orient Express” (1934), Poirot investiga un crimen en un tren atrapado por la nieve, donde cada pasajero es sospechoso.", coverUrl = "https://tse2.mm.bing.net/th/id/OIP.qET_a8U2StF1NzsE8z-s1QAAAA?rs=1&pid=ImgDetMain&o=7&rm=3", categoryId = 4, publisher = "Molino", publishDate = "1934-01-01", status = "Available", inventoryCode = "MS002"),
                BookEntity(id = 18, title = "El silencio de los corderos", author = "Thomas Harris", isbn = "Thomas Harris (1940) es un novelista estadounidense famoso por crear al icónico asesino Hannibal Lecter.\n" +
                        "\n" +
                        "Sobre el libro: “El silencio de los corderos” (1988) cuenta cómo la agente Clarice Starling busca la ayuda de Lecter para atrapar a otro asesino serial. Una mezcla de terror psicológico y suspenso.", coverUrl = "https://www.izicomics.com/wp-content/uploads/2020/03/descargar-libro-el-silencio-de-los-corderos-en-pdf-epub-mobi-o-leer-online.jpg", categoryId = 4, publisher = "Debolsillo", publishDate = "1988-05-29", status = "Available", inventoryCode = "MS003"),
                BookEntity(id = 19, title = "It", author = "Stephen King", isbn = "Stephen King (1947) es uno de los autores más prolíficos del siglo XX, conocido como el “Rey del Terror”. Sus obras combinan horror, nostalgia y humanidad.\n" +
                        "\n" +
                        "Sobre el libro: “It” (1986) narra cómo un grupo de amigos enfrenta a una entidad que adopta sus peores miedos en la ciudad de Derry. Es una historia sobre la infancia, el trauma y el mal.", coverUrl = "https://imagessl3.casadellibro.com/a/l/t0/93/9788497593793.jpg", categoryId = 4, publisher = "Debolsillo", publishDate = "1986-09-15", status = "Loaned", inventoryCode = "MS004"),
                BookEntity(id = 20, title = "Los hombres que no amaban a las mujeres", author = "Stieg Larsson", isbn = "Stieg Larsson (1954–2004) fue un periodista y novelista sueco especializado en temas de corrupción y violencia de género.\n" +
                        "\n" +
                        "Sobre el libro: “Los hombres que no amaban a las mujeres” (2005) combina periodismo, crimen y crítica social en una investigación sobre una desaparición que revela oscuros secretos familiares.", coverUrl = "https://tse1.mm.bing.net/th/id/OIP.mDYTAHVvbhrsaLIllZo39gHaLQ?rs=1&pid=ImgDetMain", categoryId = 4, publisher = "Destino", publishDate = "2005-08-23", status = "Available", inventoryCode = "MS005")
            )
        )
    }

    suspend fun searchBooks(query: String): List<BookEntity> {
        // Simula la búsqueda en la lista de libros hardcodeada
        return getCategorizedBooks().values.flatten().filter {
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }
    }
}