<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <div class="alert alert-warning" role="alert">
          Les informations de cette section seront affichées sur la page d'accueil.
        </div>

        <form name="form-description" action="<@spring.url '/admin' />" method="POST" class="border rounded p-4">

                <h5>Obligatoires</h5>
                <div class="mb-3">
                  <label for="form-description-sitename" class="form-label">Nom du site</label>
                  <input autocomplete="off" required="true" name="sitename" value="${formdata.sitename}" type="text" class="form-control" id="form-description-sitename" placeholder="biketeam">
                </div>
                <div class="mb-3">
                  <label for="form-description-description" class="form-label">Description</label>
                  <textarea required="true" name="description" class="form-control" id="form-description-description" rows="3">${formdata.description}</textarea>
                </div>
                <hr/>
                <h5>Complémentaires</h5>
                <div class="alert alert-info" role="alert">
                   Les informations ci dessous seront affichées sur la page d'accueil.
                 </div>
                <div class="mb-3">
                  <label for="form-description-facebook" class="form-label">Lien facebook</label>
                  <input autocomplete="off" name="facebook" value="${formdata.facebook}" type="text" class="form-control" id="form-description-facebook" placeholder="https://www.facebook.com/biketeam">
                </div>
                <div class="mb-3">
                  <label for="form-description-twitter" class="form-label">Lien twitter</label>
                  <input autocomplete="off" name="twitter" value="${formdata.twitter}" type="text" class="form-control" id="form-description-twitter" placeholder="https://www.twitter.com/biketeam">
                </div>
                <div class="mb-3">
                  <label for="form-description-email" class="form-label">Email</label>
                  <input autocomplete="off" name="email" value="${formdata.email}" type="email" class="form-control" id="form-description-email" placeholder="exemple@email.com">
                </div>
                <div class="mb-3">
                  <label for="form-description-phoneNumber" class="form-label">Téléphone</label>
                  <input autocomplete="off" name="phoneNumber" value="${formdata.phoneNumber}" type="text" class="form-control" id="form-description-phoneNumber" placeholder="0123456789">
                </div>
                <div class="mb-3">
                  <label for="form-description-addressStreetLine" class="form-label">Adresse</label>
                  <input autocomplete="off" name="addressStreetLine" value="${formdata.addressStreetLine}" type="text" class="form-control" id="form-description-addressStreetLine" placeholder="10 rue de la haut">
                </div>
                <div class="mb-3">
                  <label for="form-description-postalCode" class="form-label">Code postal</label>
                  <input autocomplete="off" name="addressPostalCode" value="${formdata.addressPostalCode}" type="text" class="form-control" id="form-description-postalCode" placeholder="12000">
                </div>
                <div class="mb-3">
                  <label for="form-description-city" class="form-label">Ville</label>
                  <input autocomplete="off" name="addressCity" value="${formdata.addressCity}" type="test" class="form-control" id="form-description-city" placeholder="Paris">
                </div>
                <div class="mb-3">
                  <label for="form-description-other" class="form-label">Informations libres à afficher</label>
                  <textarea name="other" class="form-control" id="form-description-other" rows="3">${formdata.other}</textarea>
                </div>
                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">