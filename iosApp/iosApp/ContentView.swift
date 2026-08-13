import Foundation
import Shared
import SwiftUI

@MainActor
final class ListaObservable: ObservableObject {
    @Published var state: ListaState
    private let vm: ListaViewModel
    private var observador: Observador?

    init() {
        vm = AppContainer().listaViewModel()
        state = vm.estadoActual()
        observador = vm.observar { [weak self] nuevo in
            DispatchQueue.main.async { self?.state = nuevo }
        }
    }

    deinit {
        observador?.cancelar()
        vm.clearForIos()
    }

    func buscar(_ texto: String) { vm.onQueryChange(query: texto) }
    func siguiente() { vm.cargarSiguientePagina() }
    func refrescar() { vm.refrescar() }
    func reintentar() { vm.reintentar() }
    func favorito(_ id: String) { vm.alternarFavorito(id: id) }
    func esFavorito(_ id: String) -> Bool { vm.esFavorito(id: id) }
    func ordenarPuntuacion() { vm.ordenarMetrica() }
    func ordenarTitulo() { vm.ordenarTitulo() }
    func ordenarFecha() { vm.ordenarFecha() }
    func error() -> String? { vm.mensajeError() }
}

@MainActor
final class DetalleObservable: ObservableObject {
    @Published var state: DetalleState
    private let vm: DetalleViewModel
    private var observador: Observador?

    init(id: String) {
        vm = AppContainer().detalleViewModel()
        state = vm.estadoActual()
        observador = vm.observar { [weak self] nuevo in
            DispatchQueue.main.async { self?.state = nuevo }
        }
        vm.cargar(id: id)
    }

    deinit {
        observador?.cancelar()
        vm.clearForIos()
    }

    func reintentar() { vm.reintentar() }
    func favorito() { vm.alternarFavorito() }
    func error() -> String? { vm.mensajeError() }
}

@MainActor
final class FavoritosObservable: ObservableObject {
    @Published var state: FavoritosState
    private let vm: FavoritosViewModel
    private var observador: Observador?

    init() {
        vm = AppContainer().favoritosViewModel()
        state = vm.estadoActual()
        observador = vm.observar { [weak self] nuevo in
            DispatchQueue.main.async { self?.state = nuevo }
        }
    }

    deinit {
        observador?.cancelar()
        vm.clearForIos()
    }

    func favorito(_ id: String) { vm.alternarFavorito(id: id) }
    func reintentar() { vm.reintentar() }
    func error() -> String? { vm.mensajeError() }
}

@MainActor
final class AjustesObservable: ObservableObject {
    @Published var state: AjustesState
    private let vm: AjustesViewModel
    private var observador: Observador?

    init() {
        vm = AppContainer().ajustesViewModel()
        state = vm.estadoActual()
        observador = vm.observar { [weak self] nuevo in
            DispatchQueue.main.async { self?.state = nuevo }
        }
    }

    deinit {
        observador?.cancelar()
        vm.clearForIos()
    }

    func tema(_ nombre: String) { vm.cambiarTema(nombre: nombre) }
    func limpiar() { vm.limpiarCache() }
}

struct ContentView: View {
    @StateObject private var ajustes = AjustesObservable()

    var body: some View {
        TabView {
            NavigationStack { ListaView() }
                .tabItem { Label("Anime", systemImage: "list.bullet") }
            NavigationStack { FavoritosView() }
                .tabItem { Label("Favoritos", systemImage: "star") }
            NavigationStack { AjustesView(model: ajustes) }
                .tabItem { Label("Ajustes", systemImage: "gear") }
        }
        .preferredColorScheme(esOscuro)
    }

    private var esOscuro: ColorScheme? {
        switch ajustes.state.tema.name {
        case "OSCURO": return .dark
        case "CLARO": return .light
        default: return nil
        }
    }
}

struct ListaView: View {
    @StateObject private var model = ListaObservable()
    @State private var texto = ""

    var body: some View {
        Group {
            if model.state.cargando && model.state.items.isEmpty {
                ProgressView()
            } else if let mensaje = model.error() {
                ErrorView(mensaje: mensaje, reintentar: model.reintentar)
            } else if model.state.items.isEmpty {
                EstadoVacio(texto: "No hay resultados")
            } else {
                lista
            }
        }
        .navigationTitle("AnimeKMP")
        .searchable(text: $texto, prompt: "Buscar anime")
        .onChange(of: texto) { nuevo in model.buscar(nuevo) }
        .toolbar { ToolbarItem(placement: .topBarTrailing) { MenuOrden(model: model) } }
    }

    private var lista: some View {
        List {
            if model.state.sinConexion { Text("Sin conexión: mostrando datos guardados") }
            ForEach(model.state.items, id: \.id) { item in
                NavigationLink(value: item.id) {
                    ItemRowView(item: item, favorito: model.esFavorito(item.id)) {
                        model.favorito(item.id)
                    }
                }
                .onAppear {
                    if item.id == model.state.items.last?.id { model.siguiente() }
                }
            }
            if model.state.cargandoMas { ProgressView() }
            if !model.state.hayMas { Text("No hay más") }
        }
        .refreshable { model.refrescar() }
        .navigationDestination(for: String.self) { DetalleView(id: $0) }
    }
}

struct MenuOrden: View {
    @ObservedObject var model: ListaObservable

    var body: some View {
        Menu("Ordenar") {
            Button("Puntuación", action: model.ordenarPuntuacion)
            Button("Título", action: model.ordenarTitulo)
            Button("Fecha", action: model.ordenarFecha)
        }
    }
}

struct ItemRowView: View {
    let item: Item
    let favorito: Bool
    let onFavorito: () -> Void

    var body: some View {
        HStack {
            AsyncImage(url: URL(string: item.imagenUrl ?? "")) { imagen in
                imagen.resizable().scaledToFill()
            } placeholder: { Color.secondary.opacity(0.15) }
            .frame(width: 70, height: 100).clipped()
            .accessibilityLabel("Portada de \(item.titulo)")
            VStack(alignment: .leading) {
                Text(item.titulo).font(.headline)
                if let subtitulo = item.subtitulo { Text(subtitulo) }
                if let metrica = item.metrica { Text("Puntuación: \(metrica)") }
            }
            Spacer()
            Button(favorito ? "★" : "☆", action: onFavorito)
                .buttonStyle(.borderless)
                .accessibilityLabel(favorito ? "Quitar favorito" : "Agregar favorito")
        }
    }
}

struct DetalleView: View {
    @StateObject private var model: DetalleObservable

    init(id: String) {
        _model = StateObject(wrappedValue: DetalleObservable(id: id))
    }

    var body: some View {
        Group {
            if model.state.cargando { ProgressView() }
            else if let mensaje = model.error() {
                ErrorView(mensaje: mensaje, reintentar: model.reintentar)
            } else if let detalle = model.state.detalle {
                ScrollView { DetalleContenido(detalle: detalle, favorito: model.state.esFavorito, onFavorito: model.favorito) }
            } else { EstadoVacio(texto: "Sin datos") }
        }
        .navigationTitle("Detalle")
    }
}

struct DetalleContenido: View {
    let detalle: ItemDetalle
    let favorito: Bool
    let onFavorito: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            AsyncImage(url: URL(string: detalle.item.imagenUrl ?? "")) { imagen in
                imagen.resizable().scaledToFit()
            } placeholder: { ProgressView() }
            .accessibilityLabel("Portada grande de \(detalle.item.titulo)")
            HStack {
                Text(detalle.item.titulo).font(.title2).bold()
                Spacer()
                Button(favorito ? "★" : "☆", action: onFavorito)
            }
            ForEach(Array(detalle.atributos.enumerated()), id: \.offset) { _, atributo in
                Text("\(atributo.etiqueta): \(atributo.valor)")
            }
            Text(detalle.descripcion.isEmpty ? "Sin descripción" : detalle.descripcion)
        }
        .padding()
    }
}

struct FavoritosView: View {
    @StateObject private var model = FavoritosObservable()

    var body: some View {
        Group {
            if model.state.cargando { ProgressView() }
            else if let mensaje = model.error() {
                ErrorView(mensaje: mensaje, reintentar: model.reintentar)
            } else if model.state.vacio {
                EstadoVacio(texto: "No tienes favoritos")
            } else {
                List(model.state.items, id: \.id) { item in
                    NavigationLink(value: item.id) {
                        ItemRowView(item: item, favorito: true) { model.favorito(item.id) }
                    }
                }
                .navigationDestination(for: String.self) { DetalleView(id: $0) }
            }
        }
        .navigationTitle("Favoritos")
    }
}

struct AjustesView: View {
    @ObservedObject var model: AjustesObservable

    var body: some View {
        Form {
            Section("Tema") {
                Button("Sistema") { model.tema("SISTEMA") }
                Button("Claro") { model.tema("CLARO") }
                Button("Oscuro") { model.tema("OSCURO") }
            }
            Section("Caché") {
                Button(model.state.limpiando ? "Borrando..." : "Borrar caché") {
                    model.limpiar()
                }
                .disabled(model.state.limpiando)
            }
        }
        .navigationTitle("Ajustes")
    }
}

struct ErrorView: View {
    let mensaje: String
    let reintentar: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle")
            Text(mensaje)
            Button("Reintentar", action: reintentar)
        }
    }
}

struct EstadoVacio: View {
    let texto: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "tray")
            Text(texto)
        }
    }
}
