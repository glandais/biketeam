/**
 * Bootstrap 5 tags
 *
 * Turns your select[multiple] into nice tags lists
 *
 * Required Bootstrap 5 styles:
 * - badge
 * - background-color utility
 * - margin-end utility
 * - forms
 * - dropdown
 */

const ACTIVE_CLASS = "bg-light";
const VALUE_ATTRIBUTE = "data-value";

class Tags {
  /**
   * @param {HTMLSelectElement} selectElement
   */
  constructor(selectElement) {
    this.selectElement = selectElement;
    this.selectElement.style.display = "none";
    this.placeholder = this.getPlaceholder();
    this.allowNew = selectElement.dataset.allowNew ? true : false;
    this.removeFirst = selectElement.dataset.removeFirst ? true : false;

    // Create elements
    this.holderElement = document.createElement("div");
    this.containerElement = document.createElement("div");
    this.dropElement = document.createElement("ul");
    this.searchInput = document.createElement("input");

    this.holderElement.appendChild(this.containerElement);
    this.containerElement.appendChild(this.searchInput);
    this.holderElement.appendChild(this.dropElement);
    // insert after
    this.selectElement.parentNode.insertBefore(this.holderElement, this.selectElement.nextSibling);

    // Configure them
    this.configureSearchInput();
    this.configureHolderElement();
    this.configureDropElement();
    this.configureContainerElement();
    this.buildSuggestions();
  }

  /**
   * Attach to all elements matched by the selector
   * @param {string} selector
   */
  static init(selector = "select[multiple]") {
    let list = document.querySelectorAll(selector);
    for (let i = 0; i < list.length; i++) {
      let el = list[i];
      let inst = new Tags(el);
    }
  }

  /**
   * @returns {string}
   */
  getPlaceholder() {
    let firstOption = this.selectElement.querySelector("option");
    if (!firstOption) {
      return;
    }
    if (!firstOption.value) {
      let placeholder = firstOption.innerText;
      if(this.removeFirst) {
        firstOption.remove();
      }
      return placeholder;
    }
    if (this.selectElement.getAttribute("placeholder")) {
      return this.selectElement.getAttribute("placeholder");
    }
    if (this.selectElement.getAttribute("data-placeholder")) {
      return this.selectElement.getAttribute("data-placeholder");
    }
    return "";
  }

  configureDropElement() {
    this.dropElement.classList.add("dropdown-menu");

    this.dropElement.addEventListener("click", (event) => {
      event.preventDefault();
      event.stopPropagation();
    });
  }

  configureHolderElement() {
    this.holderElement.classList.add("form-control");
    this.holderElement.classList.add("dropdown");
    this.holderElement.addEventListener("click", (event) => {
      event.preventDefault();
      event.stopPropagation();
    });
  }

  configureContainerElement() {
    this.containerElement.addEventListener("click", (event) => {
      event.preventDefault();
      event.stopPropagation();
      this.searchInput.focus();
    });

    // add initial values
    let initialValues = this.selectElement.querySelectorAll("option[selected]");
    for (let j = 0; j < initialValues.length; j++) {
      let initialValue = initialValues[j];
      if (!initialValue.value) {
        continue;
      }
      this.addItem(initialValue.innerText, initialValue.value);
    }
  }

  configureSearchInput() {
    this.searchInput.type = "text";
    this.searchInput.autocomplete = false;
    this.searchInput.style.border = 0;
    this.searchInput.style.outline = 0;
    this.searchInput.style.maxWidth = "100%";

    const that = this;
    window.addEventListener('click', function(e) {
       that.hideSuggestions();
    });

    this.adjustWidth();

    this.searchInput.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();
    });

    this.searchInput.addEventListener("focus", (event) => {
      this.adjustWidth();
      this.showSuggestions();
    });

    this.searchInput.addEventListener("input", (event) => {
      this.adjustWidth();
      if (this.searchInput.value.length >= 1) {
        this.showSuggestions();
      } else {
        this.hideSuggestions();
      }
    });
    // keypress doesn't send arrow keys
    this.searchInput.addEventListener("keydown", (event) => {
      if (event.code == "Enter") {
        let selection = this.getActiveSelection();
        if (selection) {
          this.addItem(selection.innerText, selection.getAttribute(VALUE_ATTRIBUTE));
          this.resetSearchInput();
          this.hideSuggestions();
        } else {
          // We use what is typed
          if (this.allowNew) {
            this.addItem(this.searchInput.value);
            this.resetSearchInput();
            this.hideSuggestions();
          }
        }
        event.preventDefault();
        return;
      }
      if (event.code == "ArrowUp") {
        this.moveSelectionUp();
      }
      if (event.code == "ArrowDown") {
        this.moveSelectionDown();
      }
      if (event.code == "Backspace") {
        if (this.searchInput.value.length == 0) {
          this.removeLastItem();
          this.adjustWidth();
        }
      }
    });
  }

  moveSelectionUp() {
    let active = this.getActiveSelection();
    if (active) {
      let prev = active.parentNode;
      do {
        prev = prev.previousSibling;
      } while (prev && prev.style.display == "none");
      if (!prev) {
        return;
      }
      active.classList.remove(ACTIVE_CLASS);
      prev.querySelector("a").classList.add(ACTIVE_CLASS);
    }
  }

  moveSelectionDown() {
    let active = this.getActiveSelection();
    if (active) {
      let next = active.parentNode;
      do {
        next = next.nextSibling;
      } while (next && next.style.display == "none");
      if (!next) {
        return;
      }
      active.classList.remove(ACTIVE_CLASS);
      next.querySelector("a").classList.add(ACTIVE_CLASS);
    }
  }

  /**
   * Adjust the field to fit its content
   */
  adjustWidth() {
    if (this.searchInput.value) {
      this.searchInput.size = this.searchInput.value.length + 1;
    } else {
      // Show the placeholder only if empty
      if (this.getSelectedValues().length) {
        this.searchInput.placeholder = "";
        this.searchInput.size = 1;
      } else {
        this.searchInput.size = this.placeholder.length;
        this.searchInput.placeholder = this.placeholder;
      }
    }
  }

  /**
   * Add suggestions from element
   */
  buildSuggestions() {
    let options = this.selectElement.querySelectorAll("option");
    for (let i = 0; i < options.length; i++) {
      let opt = options[i];
      if (!opt.getAttribute("value")) {
        continue;
      }
      let newChild = document.createElement("li");
      let newChildLink = document.createElement("a");
      newChild.append(newChildLink);
      newChildLink.classList.add("dropdown-item");
      newChildLink.setAttribute(VALUE_ATTRIBUTE, opt.getAttribute("value"));
      newChildLink.setAttribute("href", "#");
      newChildLink.innerText = opt.innerText;
      this.dropElement.appendChild(newChild);

      // Hover sets active item
      newChildLink.addEventListener("mouseenter", (event) => {
        this.removeActiveSelection();
        newChild.querySelector("a").classList.add(ACTIVE_CLASS);
      });

      newChildLink.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();
        this.addItem(newChildLink.innerText, newChildLink.getAttribute(VALUE_ATTRIBUTE));
        this.resetSearchInput();
        this.hideSuggestions();
      });
    }
  }

  resetSearchInput() {
    this.searchInput.value = "";
    this.adjustWidth();
  }

  /**
   * @returns {array}
   */
  getSelectedValues() {
    let selected = this.selectElement.querySelectorAll("option:checked");
    return Array.from(selected).map((el) => el.value);
  }

  /**
   * The element create with buildSuggestions
   */
  showSuggestions() {
    if (!this.dropElement.classList.contains("show")) {
      this.dropElement.classList.add("show");
    }

    // Position next to search input
    this.dropElement.style.left = this.searchInput.offsetLeft + "px";

    // Get search value
    let search = this.searchInput.value.toLocaleLowerCase();

    // Get current values
    let values = this.getSelectedValues();

    // Filter the list according to search string
    let list = this.dropElement.querySelectorAll("li");
    let found = false;
    let firstItem = null;
    for (let i = 0; i < list.length; i++) {
      let item = list[i];
      let text = item.innerText.toLocaleLowerCase();
      let link = item.querySelector("a");

      // Remove previous selection
      link.classList.remove(ACTIVE_CLASS);

      // Hide selected values
      if (values.indexOf(link.getAttribute(VALUE_ATTRIBUTE)) != -1) {
        item.style.display = "none";
        continue;
      }

      if (search.length === 0 || text.indexOf(search) !== -1) {
        item.style.display = "list-item";
        found = true;
        if (!firstItem) {
          firstItem = item;
        }
      } else {
        item.style.display = "none";
      }
    }

    // Special case if nothing matches
    if (!found) {
      this.dropElement.classList.remove("show");
    }

    // Always select first item
    if (firstItem) {
      if (this.holderElement.classList.contains("is-invalid")) {
        this.holderElement.classList.remove("is-invalid");
      }
      firstItem.querySelector("a").classList.add(ACTIVE_CLASS);
    } else {
      // No item and we don't allow new items => error
      if (!this.allowNew) {
        this.holderElement.classList.add("is-invalid");
      }
    }
  }

  /**
   * The element create with buildSuggestions
   */
  hideSuggestions(dropEl) {
    if (this.dropElement.classList.contains("show")) {
      this.dropElement.classList.remove("show");
    }
    if (this.holderElement.classList.contains("is-invalid")) {
      this.holderElement.classList.remove("is-invalid");
    }
  }

  /**
   * @returns {HTMLElement}
   */
  getActiveSelection() {
    return this.dropElement.querySelector("a." + ACTIVE_CLASS);
  }

  removeActiveSelection() {
    let selection = this.getActiveSelection();
    if (selection) {
      selection.classList.remove(ACTIVE_CLASS);
    }
  }

  removeLastItem() {
    let items = this.containerElement.querySelectorAll("span");
    if (!items.length) {
      return;
    }
    let lastItem = items[items.length - 1];
    this.removeItem(lastItem.getAttribute(VALUE_ATTRIBUTE));
  }

  /**
   * @param {string} text
   * @param {string} value
   */
  addItem(text, value) {
    if (!value) {
      value = text;
    }

    let button = document.createElement("button");
    button.setAttribute('type', 'button');
    button.classList.add('btn-close');
    button.style.padding = '0';
    button.style.margin = '0 0 0 5px';
    button.style.width = '10px';
    button.style.height = '10px';

    const that = this;
    button.addEventListener('click', function(event) {
         that.removeItem(value);
     });

    let span = document.createElement("span");
    span.classList.add("badge");
    span.classList.add("bg-secondary");
    span.classList.add("me-1");
    span.setAttribute(VALUE_ATTRIBUTE, value);
    span.innerText = text;
    span.appendChild(button);




    this.containerElement.insertBefore(span, this.searchInput);

    // update select
    let opt = this.selectElement.querySelector('option[value="' + value + '"]');
    if (opt) {
      opt.setAttribute("selected", "selected");
    } else {
      // we need to create a new option
      opt = document.createElement("option");
      opt.value = value;
      opt.innerText = text;
      opt.setAttribute("selected", "selected");
      this.selectElement.appendChild(opt);
    }

    if(this.selectElement.onchange) {
        this.selectElement.onchange();
    }

  }

  /**
   * @param {string} value
   */
  removeItem(value) {
    let item = this.containerElement.querySelector("span[" + VALUE_ATTRIBUTE + '="' + value + '"]');
    if (!item) {
      return;
    }
    item.remove();

    // update select
    let opt = this.selectElement.querySelector('option[value="' + value + '"]');
    if (opt) {
      opt.removeAttribute("selected");
    }

    if(this.selectElement.onchange) {
        this.selectElement.onchange();
    }

  }
}