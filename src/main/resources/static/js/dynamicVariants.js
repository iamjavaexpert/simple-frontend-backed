let variantCounter = 1;

// Function to add a new variant form
function addVariant() {
    const variantsList = document.getElementById('variants-list');
    variantCounter = document.querySelectorAll('.variant-form').length;
    const newIndex = variantCounter++;
    // Template for a new variant form
    const variantTemplate = `
            <div class="variant-form" data-variant-index="${newIndex}">
                <sl-card class="variant-card">
                    <div class="variant-header">
                        <h4>Variant #${newIndex + 1}</h4>
                         <sl-button
                                            type="button"
                                            variant="danger"
                                            size="medium"
                                            onclick="removeVariant(this)"

                                    >
                                        <sl-icon name="trash" style="font-size: medium"></sl-icon>
                                    </sl-button>
                    </div>

                    <div class="variant-form-grid">
                        <sl-input name="variants[${newIndex}].title"
                                 label="Variant Title"
                                 required>
                        </sl-input>

                        <sl-input name="variants[${newIndex}].sku"
                                 label="SKU"
                                 required>
                        </sl-input>

                        <sl-input name="variants[${newIndex}].price"
                                 label="Price"
                                 type="number"
                                 min="0"
                                 step="0.01"
                                 required>
                        </sl-input>

                        <sl-input name="variants[${newIndex}].option1"
                                 label="Option 1">
                        </sl-input>

                        <sl-input name="variants[${newIndex}].option2"
                                 label="Option 2">
                        </sl-input>

                       <div class="switch-box">
                        <label>Available</label>
                        <sl-switch
                            name="variants[${newIndex}].available"
                            label="Available"
                            value="true">
                        </sl-switch>
                       </div>
                    </div>
                </sl-card>
            </div>
        `;

    // Add the new variant form to the list
    variantsList.insertAdjacentHTML('beforeend', variantTemplate);
}

// Function to remove a variant form
function removeVariant(button) {
    const variantForm = button.closest('.variant-form');
    const variantsList = document.getElementById('variants-list');

    // Don't remove if it's the last variant
    if (variantsList.children.length > 1) {
        variantForm.remove();
        updateVariantNumbers();
    } else {

        // Show error message if there is only one variant
        const alert = Object.assign(document.createElement('sl-alert'), {
            variant: 'danger',
            closable: true,
            duration: 3000,
            innerHTML: `
                    <sl-icon slot="icon" name="exclamation-triangle"></sl-icon>
                    At least one variant is required
                `
        });

        document.body.append(alert);
        alert.toast();
    }
}

const successAlert = document.getElementById('success-message');
successAlert.addEventListener('sl-after-hide', () => {
    successAlert.style.display = 'none';
});
// Function to update the variant numbers after adding or removing a variant
function updateVariantNumbers() {
    variantCounter = document.querySelectorAll('.variant-form').length;
    const variants = document.querySelectorAll('.variant-form');
    variants.forEach((variant, index) => {
        variant.querySelector('h4').textContent = `Variant #${index + 1}`;
        variant.setAttribute('data-variant-index', index);


        // Update name attributes of all inputs
        const inputs = variant.querySelectorAll('[name]');
        inputs.forEach(input => {
            const oldName = input.getAttribute('name');
            const newName = oldName.replace(/variants\[\d+\]/, `variants[${index}]`);
            input.setAttribute('name', newName);
        });
    });
}

// Clear form after successful submission and reset variant counter
document.body.addEventListener('htmx:afterSwap', function (evt) {
    if (evt.detail.target.id === 'products-table') {
        document.getElementById('productForm').reset();

        // Reset variants to just one
        const variantsList = document.getElementById('variants-list');
        while (variantsList.children.length > 1) {
            variantsList.lastElementChild.remove();
        }
        variantCounter = 1;
        updateVariantNumbers();
    }
});

function handleFormResponse(event) {
    debugger;
    // Check if the request was successful (2xx status code)
    if (event.detail.successful) {
        showToast('success', 'Product added successfully!');
    } else {
        // You can access the error response if needed
        const errorResponse = event.detail.xhr.response;
        showToast('error', 'Failed to add product: ' + (errorResponse || 'Unknown error'));
    }
}

function showToast(variant, message) {
    const toast = Object.assign(document.createElement('sl-alert'), {
        variant: variant,
        closable: true,
        innerHTML: `
            <sl-icon slot="icon" name="${variant === 'success' ? 'check2-circle' : 'exclamation-triangle'}"></sl-icon>
            ${message}
        `
    });

    document.body.appendChild(toast);
    toast.toast();

    setTimeout(() => {
        toast.hide();
    }, 3000);
}