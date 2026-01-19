# Registration Form Specification (EU Expansion)

This document outlines the requirements for the updated registration form as the platform expands from Brazil to the European Union, headquartered in Portugal.

---

## 1. User Interface Structure

### Section A: Identity & Contact
* **Full Name (Nome Completo)***
    * *Validation:* Required.
* **Email Address (E-mail)***
    * *Validation:* Required. Must follow RFC 5322 standard.
* **Confirm Email (Confirmar E-mail)***
    * *Validation:* Must match Email Address field.
* **Phone Number (Telemóvel)***
    * *Format:* International E.164 format (e.g., +351 for Portugal). 
    * *UI Hint:* Include a country-code prefix dropdown.

### Section B: European Legal Identification
> **Note:** The Brazilian CPF is replaced by the **NIF** (Portugal) or the equivalent **TIN** (Tax Identification Number) of the user's specific EU member state.

* **Country of Residence (País de Residência)***
    * *UI:* Searchable dropdown of EU Member States.
* **Tax Identification Number (NIF / TIN)**
    * *Description:* Número de Identificação Fiscal.
    * *Validation:* Logic varies per country (e.g., Portugal uses 9 digits).

### Section C: Professional Verification
* **Professional Registration (Registo Profissional)**
    * *Label:* "Are you registered with a professional regulatory body?"
    * *Options:* [Yes] / [No]
* **Regulatory Body (Conselho / Ordem Profissional)**
    * *Example:* Ordem dos Médicos, Ordem dos Enfermeiros, etc.
* **Professional ID Number (Número de Cédula)**
    * *Validation:* Required if "Yes" is selected.

### Section D: Security
* **Password (Senha)***
* **Confirm Password (Confirmar Senha)***
    * *Requirements:* * Minimum 8 characters.
        * At least 1 numerical digit.
        * At least 1 special character (e.g., @, #, $, %).

---

## 2. GDPR & Legal Compliance
Under EU General Data Protection Regulation (GDPR), the following are mandatory:

* **[ ] Terms of Use & Privacy Policy***
    * Checkbox must be **unchecked** by default.
    * Must link to the specific Privacy Policy document explaining data residency in Portugal.
* **[ ] Data Processing Consent***
    * Explicit consent for processing health/professional data.
* **[ ] Marketing Opt-in (Optional)**
    * Separate from the legal terms.

---

## 3. Data Model Mapping (Backend)

| Legacy Field (BR) | New Field (EU) | Type | Description |
| :--- | :--- | :--- | :--- |
| `cpf` | `tax_id` | String | Stores NIF or TIN |
| `telefone` | `phone_intl` | String | Stores number with +prefix |
| `conselho` | `regulatory_order` | String | Name of the European Order |
| N/A | `country_code` | String | ISO 3166-1 alpha-2 (e.g., PT, ES) |

---

## 4. Error Messaging (Localization)

* **Required Field:** "Campo obrigatório." / "Field is required."
* **Invalid Phone:** "Número inválido. Use o formato internacional (+351)."
* **Password Weak:** "A senha deve ter 8 caracteres, 1 número e 1 caráter especial."
* **GDPR Warning:** "Deve concordar com as políticas de privacidade para prosseguir."
