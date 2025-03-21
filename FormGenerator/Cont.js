document.getElementById("fetchFormButton").addEventListener("click", fetchForm);

function fetchForm() {
    const userData = document.getElementById("userQuery").value;
    fetch("http://localhost:8080/generate-form", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ prompt: userData }) 
    })
    .then(response => response.json())
    .then(data => {
        console.log("API Response:", data); // Debugging output

        if (!data || !data.fields) {
            console.error("Invalid API response: Missing 'fields' array.");
            return;
        }

        const formContainer = document.getElementById("formContainer");
        formContainer.innerHTML = ""; // Clear previous form content

        // Create title
        const title = document.createElement("h2");
        title.textContent = data.formTitle || "Untitled Form";
        formContainer.appendChild(title);

        // Create description
        if (data.description) {
            const description = document.createElement("p");
            description.textContent = data.description;
            formContainer.appendChild(description);
        }

        // Create form element
        const form = document.createElement("form");
        form.setAttribute("id", "dynamicForm");

        data.fields.forEach(field => {
            const fieldWrapper = document.createElement("div");
            fieldWrapper.classList.add("form-group");

            const label = document.createElement("label");
            label.setAttribute("for", field.name);
            label.textContent = field.label;

            let input;

            if (field.type === "select" && field.options) {
                input = document.createElement("select");
                input.name = field.name;
                input.id = field.name;

                field.options.forEach(option => {
                    const optionElement = document.createElement("option");
                    optionElement.value = option.value;
                    optionElement.textContent = option.label;
                    input.appendChild(optionElement);
                });
            } else if (field.type === "textarea") {
                input = document.createElement("textarea");
                input.name = field.name;
                input.id = field.name;
                input.rows = 4;
            } else if (field.type === "checkbox" && field.options) {
                input = document.createElement("div");
                field.options.forEach(option => {
                    const checkboxWrapper = document.createElement("div");
                    const checkboxInput = document.createElement("input");
                    checkboxInput.type = "checkbox";
                    checkboxInput.value = option.value;
                    checkboxInput.name = field.name;
                    checkboxInput.id = `${field.name}_${option.value}`;

                    const checkboxLabel = document.createElement("label");
                    checkboxLabel.textContent = option.label;
                    checkboxLabel.setAttribute("for", `${field.name}_${option.value}`);

                    checkboxWrapper.appendChild(checkboxInput);
                    checkboxWrapper.appendChild(checkboxLabel);
                    input.appendChild(checkboxWrapper);
                });
            } else {
                input = document.createElement("input");
                input.type = field.type;
                input.name = field.name;
                input.id = field.name;
                if (field.required) input.required = true;
            }

            fieldWrapper.appendChild(label);
            fieldWrapper.appendChild(input);
            form.appendChild(fieldWrapper);
        });

        // Submit button
        const submitButton = document.createElement("button");
        submitButton.textContent = "Submit";
        submitButton.setAttribute("type", "submit");
        form.appendChild(submitButton);

        formContainer.appendChild(form);
    })
    .catch(err => console.error("Error fetching form:", err));
}



