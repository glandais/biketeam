<ul class="col-12 col-md-4 list-group mb-4">
  <a href="<@spring.url '/admin' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin'> list-group-item-secondary</#if>">Description</a>
  <a href="<@spring.url '/admin/configuration' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/configuration'> list-group-item-secondary</#if>">Configuration</a>
  <a href="<@spring.url '/admin/logo' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/logo'> list-group-item-secondary</#if>">Logo</a>
  <a href="<@spring.url '/admin/users' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/users'> list-group-item-secondary</#if>">Utilisateurs</a>
  <a href="<@spring.url '/admin/publications' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?starts_with('/admin/publications')> list-group-item-secondary</#if>">Publications</a>
  <a href="<@spring.url '/admin/rides' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?starts_with('/admin/rides')> list-group-item-secondary</#if>">Rides</a>
  <a href="<@spring.url '/admin/maps' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?starts_with('/admin/maps')> list-group-item-secondary</#if>">Maps</a>
  <a href="<@spring.url '/admin/integration' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/integration'> list-group-item-secondary</#if>">Intégrations</a>
</ul>