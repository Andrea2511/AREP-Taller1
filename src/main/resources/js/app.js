document.addEventListener("DOMContentLoaded", async function () {
    const form = document.getElementById("myForm");
    const resultName = document.getElementById("resultName");
    const resultAge = document.getElementById("resultAge");
    const resultAnimal = document.getElementById("resultAnimal");

    // Función para realizar una solicitud GET
    function fetchData() {
        const name = "Cielo";
        const age = 12;
        const favoriteAnimal = "Cat";

        const url = `http://localhost:35000/data?name=${name}&age=${age}&favoriteAnimal=${favoriteAnimal}`;

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json(); // Suponiendo que la respuesta es en formato JSON
            })
            .then(data => {
                document.getElementById('resultName').textContent = data.name || 'Unknown';
                document.getElementById('resultAge').textContent = data.age || 'Unknown';
                document.getElementById('resultAnimal').textContent = data.favoriteAnimal || 'Unknown';
            })
            .catch(error => {
                console.error('Error al obtener los datos:', error);
            });
    }

    // Llamar a la función fetchData al cargar la página
    fetchData();

    // Manejo del formulario con POST
    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const name = document.getElementById("name").value.trim();
        const age = document.getElementById("age").value.trim();
        const animal = document.querySelector('input[name="animal"]:checked')?.value;

        if (!name || !age || !animal) {
            alert("Please fill out all fields.");
            return;
        }

        const formData = new URLSearchParams();
        formData.append("name", name);
        formData.append("age", age);
        formData.append("animal", animal);

        try {
            const response = await fetch("/data", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: formData.toString()
            });

            if (!response.ok) {
                throw new Error(`Server error: ${response.status}`);
            }

            const data = await response.json();
            resultName.textContent = data.name;
            resultAge.textContent = data.age;
            resultAnimal.textContent = data.animal;
        } catch (error) {
            console.error("Error:", error);
            alert("Failed to submit data.");
        }
    });
});