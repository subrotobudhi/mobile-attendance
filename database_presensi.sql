-- phpMyAdmin SQL Dump
-- version 4.7.7
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Apr 27, 2018 at 05:13 PM
-- Server version: 10.1.31-MariaDB
-- PHP Version: 7.0.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `id1482618_presensi`
--
CREATE DATABASE IF NOT EXISTS `id1482618_presensi` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
USE `id1482618_presensi`;
-- --------------------------------------------------------

--
-- Table structure for table `aktivitas`
--

CREATE TABLE `aktivitas` (
  `id_device` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `tanggal` date NOT NULL,
  `jam` time NOT NULL,
  `lat_cek` double NOT NULL,
  `lon_cek` double NOT NULL,
  `keterangan` varchar(30) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `aktivitas`
--

INSERT INTO `aktivitas` (`id_device`, `tanggal`, `jam`, `lat_cek`, `lon_cek`, `keterangan`) VALUES
('d9914a1bf2f48509', '2018-04-27', '22:15:53', -6.8795, 107.612, 'Cek In'),
('d9914a1bf2f48509', '2018-04-27', '23:23:30', -6.8794, 107.6119983, 'Cek In'),
('d9914a1bf2f48509', '2018-04-27', '23:53:04', -6.8794, 107.6119983, 'Cek In'),
('d9914a1bf2f48509', '2018-04-27', '23:54:45', -6.8794, 107.6119983, 'Cek In'),
('d9914a1bf2f48509', '2018-04-27', '23:57:46', -6.8794, 107.6119983, 'Cek In');

-- --------------------------------------------------------

--
-- Table structure for table `pegawai`
--

CREATE TABLE `pegawai` (
  `id_pegawai` varchar(6) COLLATE utf8_unicode_ci NOT NULL,
  `nama_pegawai` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `lat_kantor` double NOT NULL,
  `lon_kantor` double NOT NULL,
  `id_device` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `status` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `pegawai`
--

INSERT INTO `pegawai` (`id_pegawai`, `nama_pegawai`, `lat_kantor`, `lon_kantor`, `id_device`, `status`) VALUES
('111111', 'Tester satu', -6.879403, 107.612, 'd9914a1bf2f48509', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `pegawai`
--
ALTER TABLE `pegawai`
  ADD PRIMARY KEY (`id_pegawai`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
