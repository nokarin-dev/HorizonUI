![HorizonUI](https://github.com/nokarin-dev/horizonui/blob/main/assets/HorizonUI_Banner.png?raw=true)

<div align="center">

[![Github](https://img.shields.io/github/downloads/nokarin-dev/horizonui/total?logo=github&labelColor=grat&color=black)](https://github.com/nokarin-dev/horizonui/releases)
[![Modrinth](https://img.shields.io/badge/dynamic/json?color=158000&label=downloads&prefix=+%20&query=downloads&url=https://api.modrinth.com/v2/project/90mpuiZs&logo=modrinth)](https://modrinth.com/mod/horizonui)
[![CurseForge](https://cf.way2muchnoise.eu/full_999424_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/horizonui)
![Environment](https://img.shields.io/badge/Enviroment-Client-purple)
![Codacy Badge](https://app.codacy.com/project/badge/Grade/7a1df9d347724bdb9ca09869f5ad517e)

[![License](https://img.shields.io/badge/License-ARR-green)](https://github.com/nokarin-dev/HorizonUI/blob/main/LICENSE)
[![Issues](https://img.shields.io/github/issues/nokarin-dev/horizonui)](https://github.com/nokarin-dev/HorizonUI/issues)
[![Release](https://img.shields.io/badge/release-v1.0.0_beta.2-orange)](https://modrinth.com/project/horizonui/versions)
[![Crowdin](https://badges.crowdin.net/horizonui/localized.svg)](https://crowdin.com/project/horizonui)
[![Github Star](https://img.shields.io/github/stars/nokarin-dev/horizonui)](https://github.com/nokarin-dev/horizonui)

</div>

Hadirkan antarmuka pengguna yang modern, bersih, dan dapat disesuaikan untuk Minecraft.
HorizonUI berfokus pada kejernihan visual, latar belakang animasi, dan penyempurnaan tata letak
untuk menghadirkan pengalaman antarmuka pengguna yang segar dan imersif.

---

> [!IMPORTANT]
> Proyek ini tidak berafiliasi dengan, didukung oleh, atau terkait dengan game, perusahaan, atau merek pihak ketiga mana pun.

> [!TIP]
> Mencari README yang diterjemahkan? Anda dapat menemukannya [di sini](https://github.strivo.xyz/HorizonUI-download/tree/main/i18n/readme)

---

![Installation](https://github.com/nokarin-dev/horizonui/blob/main/assets/Installation_Banner.png?raw=true)

1. Unduh berkas HorizonUI .jar dari salah satu platform tepercaya di bawah ini:
    - [CurseForge]
    - [Modrinth]

2. Pindahkan berkas yang telah diunduh ke folder mod Minecraft Anda:
    - Windows: C:\Users\<NamaAnda>\AppData\Roaming\.minecraft\mods
    - Linux/macOS: ~/.minecraft/mods

3. Jalankan Minecraft menggunakan mod loader yang didukung.

HorizonUI akan aktif secara otomatis saat game dimulai

> **Menggunakan peluncur kustom?** MultiMC, Prism Launcher, GDLauncher, dan peluncur kustom lainnya sepenuhnya didukung. Cukup tambahkan mod ke folder mods instance atau impor melalui pengelola mod bawaan peluncur.

---

![Usage](https://github.com/nokarin-dev/horizonui/blob/main/assets/Usage_Banner.png?raw=true)

Setelah diinstal, HorizonUI terintegrasi langsung ke antarmuka pengguna (UI) game.

Anda dapat mengonfigurasi HorizonUI melalui:
- Layar Konfigurasi Mod dalam game
- Paket Sumber Daya untuk penyesuaian latar belakang

Perubahan diterapkan secara real-time atau setelah layar dimuat ulang, tergantung pada pengaturan.

---

![Compability](https://github.com/nokarin-dev/horizonui/blob/main/assets/Compability_Banner.png?raw=true)

HorizonUI sangat berfokus pada kustomisasi antarmuka pengguna (UI), termasuk struktur tata letak dan rendering latar belakang. Akibatnya, **mod yang juga memodifikasi antarmuka pengguna Minecraft atau sistem latar belakang mungkin menimbulkan konflik**.

**Sumber ketidakcocokan potensial meliputi**:
- Mod yang merombak atau mengganti menu utama atau HUD
- Mod shader yang memengaruhi lapisan UI 2D atau pemrosesan pasca
- Mod yang mengubah cara tekstur latar belakang dimuat atau dirender

**Jika Anda mengalami masalah**:
- Nonaktifkan sementara mod lain yang terkait dengan UI.
- Periksa apakah paket shader memengaruhi rendering UI.
- Restart game setelah memodifikasi paket sumber daya.
- Laporkan konflik yang dikonfirmasi di pelacak masalah GitHub.

---

![Recommendations](https://github.com/nokarin-dev/horizonui/blob/main/assets/Recommendations_Banner.png?raw=true)

Untuk mendapatkan pengalaman visual terbaik dengan HorizonUI, sangat disarankan untuk menggunakan **mod kustomisasi font** yang menggantikan font default Minecraft.

Mod yang disarankan:
- [BetterFonts](https://www.curseforge.com/minecraft/mc-mods/betterfonts)
- [Smooth Font](https://www.curseforge.com/minecraft/mc-mods/smooth-font)
- [Caxton](https://modrinth.com/mod/caxton)

Penggunaan font modern seperti **Poppins**, **Inter**, atau **Segoe UI** secara signifikan meningkatkan keterbacaan dan konsistensi antarmuka pengguna secara keseluruhan.

---

![FAQ](https://github.com/nokarin-dev/horizonui/blob/main/assets/FAQ_Banner.png?raw=true)

<details>
<summary><strong>Buka Disini</strong></summary>

- **Mengapa latar belakang animasi tidak muncul?**  
  - Latar belakang animasi didistribusikan secara terpisah sebagai paket sumber daya untuk menjaga mod utama tetap ringan dan responsif. Anda dapat mengunduhnya dari [Han's Official Discord Server] atau membuatnya secara manual.

- **Saya sudah mengunduh latar belakang animasi, tetapi masih tidak muncul di layar judul. Mengapa?**
  - Pastikan Anda telah mengaktifkan paket sumber daya latar belakang di menu _Resource Packs_. Kemudian, buka layar konfigurasi mod untuk memilih latar belakang yang diinginkan.

- **Apa persyaratan sistem minimum untuk menjalankan mod ini dengan lancar?**
  - Kami merekomendasikan mengalokasikan setidaknya 4–6 GB RAM, dan menggunakan prosesor setara Intel Core i5 Generasi ke-8 atau lebih baik dengan GPU terintegrasi.

- **Di mana saya dapat melaporkan bug atau masalah?**  
  - Silakan laporkan bug atau masalah di [Repositori GitHub HorizonUI].

- **Apakah HorizonUI kompatibel dengan mod UI atau shader lainnya?**  
  - HorizonUI bertujuan untuk kompatibel dengan sebagian besar mod, tetapi mod UI atau shader mungkin bertabrakan. Jika Anda menemukan masalah, coba nonaktifkan mod lain sementara atau laporkan konflik kepada kami.

- **Bagaimana cara mereset pengaturan HorizonUI ke default?**
  - Anda dapat menghapus file konfigurasi HorizonUI yang terdapat di folder `config/horizonui` dalam direktori Minecraft Anda.

- **Apakah HorizonUI mendukung versi Minecraft yang lebih lama?**
  - Ya, tetapi pembaruan dirilis secara bertahap per versi. Jika versi Anda belum didukung, harap tunggu pembaruan mendatang.

</details>

---

![Credits](https://github.com/nokarin-dev/horizonui/blob/main/assets/Credits_Banner.png?raw=true)

Terima kasih kepada semua yang telah berkontribusi pada HorizonUI:
- **nokarin** (Pengembang Mod, Penguji Kode, Desainer Antarmuka Pengguna, Penerjemah, Penguji Mod)
- **Han's** (Penemu, Penerjemah, Desainer Antarmuka Pengguna)
- **AetherLumine** (Penerjemah)
- **howtoscriptinpython** (Penerjemah untuk Rusia & Ukraina)
- **ExplerHD** (Penguji Mod)
- **noxzym** (Penguji Mod)
- **alfaruqi** (Penerjemah untuk arab, brazil, mexico, italia, jerman, and georgia)
- **yunfan_adofai** (Penerjemah untuk chinese)

---

![License](https://github.com/nokarin-dev/horizonui/blob/main/assets/License_Banner.png?raw=true)

```
HorizonUI: Java Edition dilisensikan di bawah Lisensi Hak Cipta Penuh.
> © 2026 Han's Projects.
> © 2026 HorizonUI: Java Edition - All Rights Reserved.
> © 2019-2026 Strivo Development - All Rights Reserved.
```

[Modrinth]: https://modrinth.com/mod/horizonui
[CurseForge]: https://www.curseforge.com/minecraft/mc-mods/horizonui
[Website]: https://strivo.xyz/project/HorizonUI/download
[HorizonUI Github Repository]: https://github.com/nokarin-dev/HorizonUI/issues
[Han's Official Discord Server]: https://discord.com/invite/PgfBrGrd9b